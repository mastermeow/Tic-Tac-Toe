import 'core-js/stable'; /*the 'core-js/stable' module must be at the top of your entry point*/
import 'regenerator-runtime/runtime';
import React from 'react';
import ReactDOM from 'react-dom';
import axios from 'axios';

export default class TicTacToe extends React.Component {

    constructor(props) {
    	super(props);
    	this.urlResetGame = '/tictactoes/reset-game';
    	this.urlSaveMove = '/tictactoes/save-move';
    	this.urlViewPastMove = '/tictactoes/view-prev-move';
    	this.urlRevertToPrevMove = '/tictactoes/revert-to-prev-move';
        this.urlSaveRecord = '/players/save-record';

    	this.state = {
    	    winner: null, /*winner = 'X' or 'O' if game is over; null if not. */
    	    xNext: true,  /*true if it's X's turn; false if is O's turn. X plays first.*/
    	    board: [[null, null, null], [null, null, null], [null, null, null]], /*an empty board of String primitives*/
    	    numOfMoves: -1, /*total number of moves made in the game so far.*/
    	    numOfMovesResetByTimeTravel: -1, /*move # reset by players when they click a time travel button.*/

    	    playerAttributes:['firstName', 'lastName', 'nickName', 'numTicTacToeDraw',
    	    'numTicTacToeLoss', 'numTicTacToeWin', 'score'], /*same as player.js*/
    	    playerX: null,
    	    playerO: null,
    	};

    	this.initPlayer = this.initPlayer.bind(this);
    }

    componentDidMount() {
        /*componentDidMount() is invoked after render() is fired.*/
        console.log('Component did mount.');
        this.initTicTacToe();
    }

    componentDidUpdate(){
        /*componentDidUpdate() is invoked immediately after updating occurs.*/
        console.log('Component did update.');
    }

    /*TicTacToe: start -------------------*/
    initTicTacToe(){
        /*First delete all the moves of the previous game, then initialize a new game.*/
        console.log('Initializing new game.');
        axios({
            method: 'post',
            url: this.urlResetGame,
        }).then(responseOfResettingGame =>{
            const ticTacToe = {
                'winner': this.state.winner,
                'xNext': this.state.xNext,
                'board': this.state.board,
            };
            axios({
                method: 'post',
                url: this.urlSaveMove,
                data: ticTacToe,
            }).then(responseOfSavingEmptyBoard =>{
                this.setState({
                    winner: responseOfSavingEmptyBoard.data.winner,
                    xNext: responseOfSavingEmptyBoard.data.xNext,
                    board: responseOfSavingEmptyBoard.data.board,
                    numOfMoves: this.state.numOfMoves+1,
                    numOfMovesResetByTimeTravel: this.state.numOfMoves+1,
                });
            }).catch(errorOfSavingEmptyBoard =>{
                console.error(errorOfSavingEmptyBoard);
            });
        }).catch(errorOfResettingGame=>{
            console.error(errorOfResettingGame);
        });
    }

    handleClick(i, j){
        console.log('Clicking grid ('+i+', '+j+') on board. ');
        /*Do nothing if a winner has come out or the grid was already clicked by a player.*/
        if(this.state.winner || this.state.board[i][j]){
            if(this.state.winner || this.isGameOver()){
                alert('The game is over! You can go to Player List to check your record :)');
            }
            return;
        }
        /*Revert to previous move if the move# has been reset by clicking a time travel button,
        then save the new move made by clicking a grid; Otherwise, save the new move directly.*/
        if(this.state.numOfMovesResetByTimeTravel < this.state.numOfMoves){
            console.log('Reverting to move #'+this.state.numOfMovesResetByTimeTravel);
            axios({
                method: 'post',
                url: this.urlRevertToPrevMove,
                params:{
                    move: this.state.numOfMovesResetByTimeTravel,
                },
            }).then(response=>{
                const snapshot = response.data;
                this.setState({
                    numOfMoves: this.state.numOfMovesResetByTimeTravel,
                });
                const board_new = snapshot.board;
                board_new[i][j] = snapshot.xNext? 'X':'O';
                const ticTacToe_new = {
                    'winner': snapshot.winner,
                    'xNext': !snapshot.xNext,
                    'board': board_new,
                };
                this.saveTicTacToe(ticTacToe_new);
            }).catch(error=>{
                console.error(error);
            });
        }else{
            const board_new = this.state.board;
            board_new[i][j] = this.state.xNext? 'X':'O';
            const ticTacToe_new = {
                'winner': this.state.winner,
                'xNext': !this.state.xNext,
                'board': board_new,
            };
            this.saveTicTacToe(ticTacToe_new);
        }
    }

    viewPastMove(pastMove){
        /*re-load data of a past move.*/
        console.log('Viewing move #'+pastMove);
        axios({
            method: 'post',
            url: this.urlViewPastMove,
            params:{
                move: pastMove,
            }
        }).then(response=>{
            const snapshot = response.data;
            this.setState({
                winner: snapshot.winner,
                xNext: snapshot.xNext,
                board: snapshot.board,
                numOfMovesResetByTimeTravel: pastMove,
                /*Do not change numOfMoves since player may click the button to go back to the latest move.*/
            });
        }).catch(error=>{
            console.error(error);
        });
    }

    saveTicTacToe(ticTacToe){
        console.log('Saving new status of the game created by clicking a grid. ');
        axios({
            method: 'post',
            url: this.urlSaveMove,
            data: ticTacToe,
        }).then(response=>{
            /*console.log('TicTacToe: current states = \n'+JSON.stringify(response.data, null, 4));*/
            this.setState({
                winner: response.data.winner,
                xNext: response.data.xNext,
                board: response.data.board,
                /*ensure that the two numbers of moves are the same:*/
                numOfMoves: this.state.numOfMoves+1,
                numOfMovesResetByTimeTravel: this.state.numOfMoves+1,
            });
            if(this.state.winner){
                console.log('Game is over; a winner has come out. ');
                let myWinner = this.state.winner === 'X' ? this.state.playerX : this.state.playerO;
                let myLoser = this.state.winner === 'X'? this.state.playerO : this.state.playerX;
                myWinner.numTicTacToeWin = 1;
                myLoser.numTicTacToeLoss = 1;
                this.saveRecord(myWinner);
                this.saveRecord(myLoser);
            }else if(this.isGameOver()){
                console.log('Game is over; nobody wins.');
                let myDrawX = this.state.playerX;
                let myDrawO = this.state.playerO;
                myDrawX.numTicTacToeDraw = 1;
                myDrawO.numTicTacToeDraw = 1;
                this.saveRecord(myDrawX);
                this.saveRecord(myDrawO);
            }
        }).catch(error=>{
            console.error(error);
        });
    }

    /*Return true if the game is over (i.e. no more empty grid to click), false otherwise.*/
    isGameOver(){
        console.log('checking if game is over.');
        const n = this.state.board.length;
        for(let i = 0; i < n; i++){
            for(let j = 0; j < n; j++){
                if(!this.state.board[i][j]){/*i.e. board[i][j] = null*/
                    return false;
                }
            }
        }
        return true;
    }
    /*TicTacToe: end -------------------*/

    /*Player: start -------------------*/
    initPlayer(player){
        /*Initialize player X first, then initialize player O; X and O must have different names.
        Return true if successfully initialized the player; false otherwise. */
        if(!this.state.playerX){ /*if playerX is null*/
            console.log('Initializing Player X ('+player.lastName+', '+player.firstName+').');
            this.setState({
                playerX: player,
            });
            return true;
        }else if(!this.state.playerO){ /*if playerO is null*/
            console.log('Initializing Player O ('+player.lastName+', '+player.firstName+'). ');
            if(this.validPlayerNames(this.state.playerX, player)){
                this.setState({
                    playerO: player,
                });
                return true;
            }else{
                console.error('The name ('+player.lastName+', '+player.firstName
                +') is already assigned to Player X and thus cannot be assigned to Player O.');
            }
        }else{
            console.error('You are trying to initialize a player when both Player X and Player O are already initialized.');
        }
        return false;
    }

    validPlayerNames(playerX, playerO){
        /* Given PlayerX and PlayerO whose firstName & lastName don't entirely consist of white spaces.
        Return false if the two players have the same firstName & lastName; return true otherwise.*/
        console.log('Verifying if the two players of the game have different names.');
        const fX = playerX.firstName.toLowerCase().trim();
        const fO = playerO.firstName.toLowerCase().trim();
        const lX = playerX.lastName.toLowerCase().trim();
        const lO = playerO.lastName.toLowerCase().trim();
        if(fX===fO && lX ===lO){
            return false;
        }
        return true;
    }

    saveRecord(player){
        console.log('Saving record of player '+JSON.stringify(player, null, 4));
        axios({
            method: 'post',
            url: this.urlSaveRecord,
            data: player,
        }).then(response=>{
            console.log('response.data = '+JSON.stringify(response.data, null, 4));
        }).catch(error=>{
            console.error(error);
        });
    }
    /*Player: end -------------------*/

    /*render: start----------------------*/
	render(){
	    console.log('Rendering game.');
        if(!(this.state.playerX && this.state.playerO)){
            return(
                <CreatePlayer
                    playerAttributes = {this.state.playerAttributes}
                    playerX = {this.state.playerX}
                    playerO = {this.state.playerO}
                    initPlayer = {this.initPlayer}
                />
            );
        }else{
            return(
                <Game
                    playerX = {this.state.playerX} /*only for displaying the player info on top of time travel buttons*/
                    playerO = {this.state.playerO} /*only for displaying the player info on top of time travel buttons*/
                    xNext = {this.state.xNext}
                    winner = {this.state.winner}
                    board = {this.state.board}
                    numOfMoves = {this.state.numOfMoves}
                    onClick = {(i,j)=>this.handleClick(i,j)}
                    revertTo = {(prevMove) =>this.revertTo(prevMove)}
                    viewPastMove = {(pastMove) => this.viewPastMove(pastMove)}
                />
            );
        }
	}
	/*render: end----------------------*/
}

/*createPlayer: start -----------------------*/
class CreatePlayer extends React.Component{
	constructor(props) {
		super(props);
		this.refModalInputX = {}; /*refs for grabbing inputs of modal dialog (player X).*/
		this.refModalInputO = {}; /*refs for grabbing inputs of modal dialog (player O).*/
		this.handleSubmit = this.handleSubmit.bind(this);
		this.handleCancel = this.handleCancel.bind(this);
	}

	handleSubmit(e) {
		e.preventDefault();

		/*Extract data from modal dialog inputs*/
		const newPlayer = {};
		/*Create player X first, then create player O.*/
		if(!this.props.playerX){
			this.props.playerAttributes.forEach(attribute => {
    			newPlayer[attribute] = (attribute.includes('Name'))? this.refModalInputX[attribute].current.value.trim(): 0;
    		});
		}else{
			this.props.playerAttributes.forEach(attribute => {
    			newPlayer[attribute] = (attribute.includes('Name'))? this.refModalInputO[attribute].current.value.trim(): 0;
    		});
		}
		/*Ensure player's firstName & lastName are not empty or white spaces.*/
		if(newPlayer.firstName.length<1){
		    alert('First name cannot be empty or white space(s).');
		}else if(newPlayer.lastName.length<1){
		    alert('Last name cannot be empty or white spaces(s).');
		}else{
		    if(this.props.initPlayer(newPlayer)){ /*true if playerO and playerX are assigned with different names.*/
	            console.log('Successfully initialized player.');
                this.clearInputs();
                if(this.props.playerX){ /*use the nullity of playerX to determine if both players are created.*/
                    window.location = '#';
                }
		    }else{
		        alert('Player O cannot have the same first name and last name as Player X. '
		        +'Please choose a different first name and/or a different last name for Player O.');
		    }
		}
	}

	handleCancel(e){
        this.clearInputs();
	}

	clearInputs(){
	    if(!this.props.playerX){
			this.props.playerAttributes.forEach(attribute => {
    			if(attribute.includes('Name')){
                    this.refModalInputX[attribute].current.value = ''; /*clear inputs submitted for player X.*/
                }
    		});
	    }else{
			this.props.playerAttributes.forEach(attribute => {
    			if(attribute.includes('Name')){
                    this.refModalInputO[attribute].current.value = ''; /*clear inputs that are submitted for player O.*/
                }
    		});
	    }

	}

    render(){
	    /*Create refs to player attributes.*/
        this.props.playerAttributes.forEach(attribute =>{
		    this.refModalInputX[attribute] = React.createRef();
		    this.refModalInputO[attribute] = React.createRef();
		});
        /*Configure modal dialog's title & inputs.*/
        const title = !this.props.playerX? 'Create Player X': 'Create Player O';
		const inputs = this.props.playerAttributes.map(attribute =>
		    (attribute.includes('Name'))?
			<p key={attribute}> {/*key is for distinguishing between different attributes.*/}
				<input
				    type='text'
				    maxLength= '32'
				    ref= {!this.props.playerX? this.refModalInputX[attribute] : this.refModalInputO[attribute]}
				    placeholder={attribute}
				/>
			</p>
			: <div key ={attribute}></div> /*each attribute must have its <div/>, even if its input is disabled.*/
		);
        return(
            <div>
                <div>
                    <h2>
                        Tic-Tac-Toe
                    </h2>
                </div>
                <div>
                    <p className = 'game-rule'>
                        Rule: Two players (represented by X and O) alternate turns placing their mark on
                        an empty cell. The winner is the first player to form an unbroken chain of 3 marks
                        horizontally, vertically, or diagonally.
                    </p>
                </div>
                <a href='#createPlayer' className = 'buttonGreen'>Start Game</a> {/*The hyperlink directing the dialog*/}
				<div id='createPlayer' className='modalDialog'> {/*The hidden dialog.*/}
					<div align = 'left'>
					    <div>
						    <h3>{title}</h3>
						</div>
						<form>
							{inputs}
							<a href='#' className='buttonGray' onClick={this.handleCancel}>cancel</a>
							&emsp; &emsp; &ensp;
							<button className='buttonGreen' onClick={this.handleSubmit}>submit</button>
						</form>
					</div>
				</div>
            </div>
        );
    }
}
/*createPlayer: end -----------------------*/

/*Game: start --------------------*/
class Game extends React.Component {
    /*The view of playing TicTacToe.*/
    render(){
        /*display players' names */
        const playerXName = 'player X: '+this.props.playerX.lastName+', '+this.props.playerX.firstName;
        const playerOName = 'player O: '+this.props.playerO.lastName+', '+this.props.playerO.firstName;

        /*display winner/the next player. */
	    const player = this.props.xNext? 'X': 'O';
        if(this.props.winner){ /*null is treated as falsy for boolean operations.*/
            status = 'Winner: '+this.props.winner;
        }else{
            status = 'Next player: '+  player;
        }
        /*display buttons that allow players to go to previous/current moves; display no button if a player already wins.*/
        let timeTravelButtons = [];
        if(!this.props.winner){
            for(let i = 0; i <= this.props.numOfMoves; i++){
                const timeTravelButtonText = i > 0 ? 'Go to move #'+i : 'Go to start';
                timeTravelButtons.push(
                    <div key = {'move #'+i}>
                        <button
                            className = 'buttonGreen'
                            /*onClick = {()=> this.props.revertTo(i)}*/
                            onClick = {() => this.props.viewPastMove(i)}
                        >
                            {timeTravelButtonText}
                        </button>
                    </div>
                );
            }
        }

        /*render.*/
        return(
            <div align = 'center' className = 'game'>
                <h2>Tic-Tac-Toe</h2>
                <table>
                    {/*<caption>Tic-Tac-Toe</caption>*/}
                    <tbody>
                        <tr>
                            <td>
                                <p className='game-rule'>
                                    Rule: Two players (represented by X and O) alternate turns placing their mark on
                                    an empty cell. The winner is the first player to form an unbroken chain of 3 marks
                                    horizontally, vertically, or diagonally.
                                </p>
                                <Board
                                    board = {this.props.board}
                                    onClick = {(i,j) => this.props.onClick(i,j)}
                                />
                            </td>
                            <td valign = 'center'>
                                <div><p>{playerXName}</p></div>
                                <div><p>{playerOName}</p></div>
                                <div><p>{status}</p></div>
                                <div>
                                    {timeTravelButtons}
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        );
    }
}
/*Game: end --------------------*/


/*Board: start --------------------*/
class Board extends React.Component {
    renderCell(i,j) {
        return (
            <Cell
                key = {'cell('+i+','+j+')'}
                cell = {this.props.board[i][j]}
                onClick = {()=>this.props.onClick(i,j)} /*a callback function*/
            />
        );
    }

    renderBoard(){
        let board = [];
        const n = this.props.board.length;
        for(let i = 0; i < n; i++){
            let row = [];
            for(let j = 0; j < n; j++){
                row.push(this.renderCell(i, j));
            }
            board.push(<div key = {'row'+i}>{row}</div>);
        }
        return board;
    }

    render() {
        return (
            <div className ='game-board'>
                {this.renderBoard()}
            </div>
        );
   }
}
/*Board: end --------------------*/

/*Cell: start--------------------*/
class Cell extends React.Component{
    render(){
        return (
            <button
                className = 'game-cell'
                onClick = {()=> this.props.onClick()} /*a callback function*/
            >
                {this.props.cell}
            </button>
          );
    }
}
/*Cell: end----------------------*/

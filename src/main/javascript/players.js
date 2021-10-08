import 'core-js/stable';
import 'regenerator-runtime/runtime';
import React from 'react';
import ReactDOM from 'react-dom';
import axios from 'axios';

export default class Players extends React.Component {
    constructor(props) {
    	super(props);
        this.urlRepo = '/players/repository';
        this.urlCreate = '/players/create-player';
        this.urlDelete = '/players/delete-player';
        this.urlReplace = '/players/replace-player';

    	this.state = {
    	    playerAttributes:['firstName', 'lastName', 'nickName', 'numTicTacToeDraw',
    	    'numTicTacToeLoss', 'numTicTacToeWin', 'score'],
    	    players: [],   /*players to be displayed in player list*/
            pageNumber: 0, /*page number of player list*/
    	    pageSize: 8,   /*page size of player list*/
    	    totalPages: 0, /*number of pages of player list*/
    	    sortBy: 'id',  /*sorting param of player list.*/
    	};

        this.updatePageSize = this.updatePageSize.bind(this);
        this.updatePageNumber = this.updatePageNumber.bind(this);
        this.createPlayer = this.createPlayer.bind(this);
        this.deletePlayer = this.deletePlayer.bind(this);
        this.replacePlayer = this.replacePlayer.bind(this);
    }

    componentDidMount() {
        /*componentDidMount() runs after render() is fired.*/
        console.log('Component did mount.');
        this.getRepo();
    }

    componentDidUpdate(prevProps, prevState){
        /*componentDidUpdate() is invoked immediately after updating occurs.*/
        console.log('Component did update.');
        if(this.state.pageSize!==prevState.pageSize || this.state.pageNumber!==prevState.pageNumber){
            console.log('Reloading data due to a change in page size/number.');
            this.getRepo();
        }
    }

    /*CRUD operations: start -------------------*/
    getRepo(){
        console.log('Loading data from server.');
        axios({
            method: 'get',
            url: this.urlRepo,
            params:{
                page: this.state.pageNumber,
                size: this.state.pageSize,
                sortBy: this.state.sortBy,
            }
        }).then(response=>{
            this.setState({
                players: response.data.content,
                pageNumber: response.data.pageable.pageNumber,
                pageSize: response.data.pageable.pageSize,
                totalPages: response.data.totalPages,
            });
        }).catch(error=>{
            console.error(error);
        });
    }

    createPlayer(player){
        console.log('Creating new player.');
        axios({
            method: 'post',
            url: this.urlCreate,
            data: player,
        }).then(response=>{
            this.getRepo(); /*Re-render to display updates.*/
        }).catch(error=>{
            alert('Error: failed to create player; '+error.response.data);
            console.error('Error: failed to create player; '+error.response.data);
        });
    }

    deletePlayer(player){
        console.log('Deleting player.');
        axios({
            method: 'post',
            url: this.urlDelete,
            data: player,
        }).then(response =>{
            this.getRepo(); /*Re-render to display updates.*/
        }).catch(error => {
            alert('Failed to delete player: '+error.response.data);
            console.error('Failed to delete player: '+error.response.data);
        });
    }

    replacePlayer(oldPlayer, newPlayer){
        console.log('Replacing Player.');
        axios({
            method: 'post',
            url: this.urlReplace,
            data: {
                'old': oldPlayer,
                'new': newPlayer,
            },
        }).then(response=>{
            this.getRepo();
        }).catch(error=>{
            alert('Error: failed to update player; '+error.response.data);
                console.error('Error: failed to update player; '+error.response.data);
        });
    }

    updatePageSize(pageSize){
        if(this.state.pageSize!== pageSize){
            console.log('Updating page size.');
            this.setState({
                pageSize: pageSize
            });
        }
    }

    updatePageNumber(pageNumber){
        if(this.state.pageNumber!== pageNumber){
            console.log('Updating page number.');
            this.setState({
                pageNumber: pageNumber
            });
        }
    }
    /*CRUD operations: end -------------------*/

    /*render: start----------------------*/
	render(){
	    console.log('Rendering player list.');
		return (
		    <div align = 'center' className = 'players'>
		        <h2>Player List</h2>
		        <CreatePlayer
		            playerAttributes={this.state.playerAttributes}
		            createPlayer={this.createPlayer}
		        />
           		<PlayerList
           		    players={this.state.players}
           		    playerAttributes ={this.state.playerAttributes}
           		    pageNumber={this.state.pageNumber}
           			pageSize={this.state.pageSize}
           			totalPages={this.state.totalPages}

           			updatePageSize={this.updatePageSize}
           			updatePageNumber={this.updatePageNumber}
           			createPlayer={this.createPlayer}
                    deletePlayer={this.deletePlayer}
                    replacePlayer={this.replacePlayer}
           	    />
           	</div>
		)
	}
	/*render: end----------------------*/
}

/*PlayerList Component: start=====================================================================*/
class PlayerList extends React.Component {

	constructor(props) {
		super(props);
		this.refPageSize = React.createRef(); /*ref for accessing input page size.*/
		this.handleInput = this.handleInput.bind(this);
		this.handleNavFirst = this.handleNavFirst.bind(this);
        this.handleNavPrev = this.handleNavPrev.bind(this);
        this.handleNavNext = this.handleNavNext.bind(this);
        this.handleNavLast = this.handleNavLast.bind(this);
	}

    /*handle page input & navigation: start-------------------------------*/
	handleInput(e){
	    e.preventDefault();
	    console.log('Inputing new page size.');
        let pageSize = this.refPageSize.current.value;
        const numbers = /^[0-9]+$/; /*Only numbers.*/
        const pageSizeMax = 8;
        if(numbers.test(pageSize)){
            pageSize = Math.min(Math.max(pageSize,1), pageSizeMax);
        }else{
            pageSize = pageSizeMax;
        }
        this.props.updatePageSize(pageSize);
	}

    handleNavFirst(e){
		e.preventDefault();
		console.log('Go to the first page.');
		this.props.updatePageNumber(0);
	}

	handleNavPrev(e) {
		e.preventDefault();
		console.log('Go to the prev page.');
		let pageNumber = this.props.pageNumber;
		pageNumber = Math.max(pageNumber-1, 0);
		this.props.updatePageNumber(pageNumber);
	}

	handleNavNext(e) {
		e.preventDefault();
		console.log('Go to the next page.');
		let pageNumber = this.props.pageNumber;
		let lastPage = this.props.totalPages-1;
        pageNumber = pageNumber < lastPage? pageNumber+1 : lastPage;
        this.props.updatePageNumber(pageNumber);
	}

	handleNavLast(e) {
		e.preventDefault();
		console.log('Go to the last page.');
		this.props.updatePageNumber(this.props.totalPages-1);
	}
	/*handle page input & navigation: end -------------------------------*/

	render() {
	    const players = this.props.players.map(player =>
        	<Player
        	    key={player.firstName+player.lastName} /*for distinguishing between different players.*/
        	    player={player}
        	    playerAttributes = {this.props.playerAttributes} /*for UPDATE request*/
        	    replacePlayer = {this.props.replacePlayer}
        	    deletePlayer = {this.props.deletePlayer}
        	/>
        );

        /*build navigation bar: start------------------------*/
        const navLinks = [];
        navLinks.push(<button className = 'buttonGraySquare' title = 'first page' key='first' onClick={this.handleNavFirst}>&lt;&lt;</button>);
        navLinks.push(<button className = 'buttonGraySquare' title = 'prev page' key='prev' onClick={this.handleNavPrev}>&lt;</button>);
        const pageNumber = this.props.pageNumber+1; /*default pageNumber starts from 0 rather than 1.*/
        navLinks.push(' page '+pageNumber+' of '+this.props.totalPages+' ');
        navLinks.push(<button className = 'buttonGraySquare' title = 'next page' key='next' onClick={this.handleNavNext}>&gt;</button>);
        navLinks.push(<button className = 'buttonGraySquare' title = 'last page' key='last' onClick={this.handleNavLast}>&gt;&gt;</button>);

		/*build navigation bar: end------------------------*/
		return (
			<div align = 'center'>
				<table>
				    {/*<caption> Player List </caption>*/}
					<tbody>
						<tr>
							<th rowSpan='2'>First Name</th>
							<th rowSpan='2'>Last Name</th>
							<th rowSpan='2'>Nickname</th>
							<th colSpan='3'>Tic-Tac-Toe</th>
							<th rowSpan='2'>*Score</th>
							<th rowSpan='2'></th>
						</tr>
						<tr>
							<th className = 'fixedWidth40px'>Wins</th>
							<th className = 'fixedWidth40px'>Draws</th>
							<th className = 'fixedWidth40px'>Losses</th>
						</tr>
						{players}
					</tbody>
				</table>
                <div className = 'page-nav'>
                    <div>
                        *Score = [Tic-Tac-Toe] (Wins - Losses).
                    </div>
                    <div>
                    </div>
                    <span>
                        <span>
                            {navLinks}
                        </span>
                        &emsp;
                        page size:
                        &ensp;
				        <input
				            ref= {this.refPageSize}
				            defaultValue={this.props.pageSize}
				            onInput={this.handleInput}
				            placeholder= '(max = 8)'
				        />
				    </span>
				</div>
			</div>
		)
	}
}
/*PlayerList Component: end=====================================================================*/

/*Player Component: start=====================================================================*/
class Player extends React.Component {
	constructor(props) {
		super(props);
	}
	render() {
		return (
			<tr>
				<td>{this.props.player.firstName}</td>
				<td>{this.props.player.lastName}</td>
				<td>{this.props.player.nickName}</td>
				<td>{this.props.player.numTicTacToeWin}</td>
				<td>{this.props.player.numTicTacToeDraw}</td>
				<td>{this.props.player.numTicTacToeLoss}</td>
				<td>{this.props.player.score}</td>
				<td>
				     <UpdatePlayer
				         player = {this.props.player}
				         playerAttributes={this.props.playerAttributes}
				         replacePlayer={this.props.replacePlayer}
				     />
				     &emsp;
				     <DeletePlayer
				         player = {this.props.player}
				         deletePlayer={this.props.deletePlayer}
				     />
				</td>
			</tr>
		)
	}
}
/*Player Component: end =====================================================================*/

/*CreatePlayer Component: start =============================================================*/
class CreatePlayer extends React.Component {
    /*Modal dialog' inputs: firstName, lastName, nickName.*/
	constructor(props) {
		super(props);
		this.refModalInput = {}; /*(object) key-value pairs of refs for grabbing inputs of dialog.*/
		this.handleSubmit = this.handleSubmit.bind(this);
		this.handleCancel = this.handleCancel.bind(this);
	}

	handleSubmit(e) {
		e.preventDefault();
		const newPlayer = {};
		this.props.playerAttributes.forEach(attribute => {
			newPlayer[attribute] = (attribute.includes('Name'))? this.refModalInput[attribute].current.value.trim(): 0;
		});

		this.props.createPlayer(newPlayer);
    	this.clearInputs();
    	window.location = '#';

	}

	handleCancel(e){
        this.clearInputs();
	}

	clearInputs(){
		this.props.playerAttributes.forEach(attribute => {
			if(attribute.includes('Name')){
                this.refModalInput[attribute].current.value = '';
            }
		});
	}

	render() {
	    /*Create refs to player attributes.*/
        this.props.playerAttributes.forEach(attribute =>{
		    this.refModalInput[attribute] = React.createRef();
		});
        /*Configure dialog inputs.*/
		const inputs = this.props.playerAttributes.map(attribute =>
		    (attribute.includes('Name'))?
			<p key={attribute}> {/*key is for distinguishing between different attributes.*/}
				<input
				    type='text'
				    maxLength='32'
				    ref= {this.refModalInput[attribute]}
				    placeholder={attribute}
				/>
			</p>
			: <div key ={attribute}></div> /*each attribute must have its <div/>, even if its input is disabled.*/
		);

		return (
			<div > {/*Use <div> so it aligns as a block with others.*/}
                <a href='#createPlayer' className = 'buttonGreen'>Create New Player</a> {/*The hyperlink directing the dialog*/}
				<div id='createPlayer' className='modalDialog'> {/*The hidden dialog.*/}
					<div align = 'left'>
					    <div>
						    <h3>Create player</h3>
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
/*CreatePlayer Component: end ===============================================================*/

/*DeletePlayer Component: start =============================================================*/
class DeletePlayer extends React.Component {
	constructor(props) {
		super(props);
		this.handleSubmit = this.handleSubmit.bind(this);
		this.handleCancel = this.handleCancel.bind(this);
	}

	handleSubmit(e) {
		e.preventDefault();
		this.props.deletePlayer(this.props.player);
		window.location = '#'; /*Navigate away from the dialog to hide it.*/
	}

	handleCancel(e){
        /*Default event handler: close the dialog.*/
	}

	render() {
        /*For each player, <DeletePlayer> needs a unique href to the player to display its data.*/
        const param = '?firstName='+this.props.player.firstName+'&lastName='+this.props.player.lastName;
		const dialogHref = '#deletePlayer'+param; /*'#targetName'*/
		const dialogId = 'deletePlayer'+param;
	    const playerName = ' '+this.props.player.firstName+' '+this.props.player.lastName+' ';
		return (
			<span > {/*Use <span> so it aligns inline with others.*/}
                <a href={dialogHref} className = 'buttonMagenta'>Delete</a> {/*The hyperlink directing the dialog*/}
				<div id={dialogId} className='modalDialog'> {/*The hidden dialog.*/}
					<div align = 'center'>
						<form>
						    <p>
						        {/*Increase horizontal space.*/}
						    </p>
						    <p>
						        Are you sure you want to delete
						    </p>
							<p>
							    <span className = 'pink-font'>
							        {playerName}
							    </span>
							    ?
							</p>
							<a href='#' className='buttonGray' onClick={this.handleCancel}>&emsp; No&emsp;</a>
							&emsp; &emsp; &emsp; &emsp;
							<button className='buttonMagenta' onClick={this.handleSubmit}>&emsp;Yes&emsp;</button>
						</form>
					</div>
				</div>
			</span>
		)
	}

}
/*DeletePlayer Component: end ===============================================================*/

/*UpdatePlayer Component: start =============================================================*/
class UpdatePlayer extends React.Component {
    /*Modal dialog' inputs: firstName, lastName, nickName.*/
	constructor(props) {
		super(props);
		this.refModalInput = {}; /*refs for distinguishing different fields of the input.*/
		this.currentPlayer = JSON.parse(JSON.stringify(this.props.player, null, 4)); /*JSON => JavaScript object*/
		this.handleSubmit = this.handleSubmit.bind(this);
		this.handleCancel = this.handleCancel.bind(this);
	}

	handleSubmit(e) {
	    /*Prevents the default handler.*/
		e.preventDefault();

        /*Extract dialog's inputs.*/
		const newPlayer = {};
		this.props.playerAttributes.forEach(attribute => {
		    newPlayer[attribute] = (attribute.includes('Name'))?
			    this.refModalInput[attribute].current.value.trim() : this.currentPlayer[attribute];
		});

		/*Replace player*/
		this.props.replacePlayer(this.props.player, newPlayer);

        /*Update default inputs of the modal dialog.*/
		this.resetInputs();

        /*Navigate away from the dialog to hide it.*/
		window.location = '#';
	}

	handleCancel(e){
        this.resetInputs();
	}

	resetInputs(){
        /*Reset the default value of the inputs of the modal dialog.*/
        this.props.playerAttributes.forEach(attribute => {
			if(attribute.includes('Name')){
                this.refModalInput[attribute].current.value = this.currentPlayer[attribute];
            }
		});
	}

	render() {
	    /*Create refs to player attributes.*/
        this.props.playerAttributes.forEach(attribute =>{
		    this.refModalInput[attribute] = React.createRef();
		});

        /*Configure dialog inputs.*/
		const inputs = this.props.playerAttributes.map(attribute =>
			<p key={attribute}> {/*key is for distinguishing between different playerAttributes of a player.*/}
				<input
				    type='text'
				    maxLength='32'
				    ref= {this.refModalInput[attribute]}
				    defaultValue={this.currentPlayer[attribute]}
				    disabled = {!attribute.includes('Name')} /*No cheat in the score ;)*/
				/>
			</p>
		);

        /*For each player, <Replace Player> needs a unique href to the player to display its data.*/
        const param='?firstName='+this.props.player.firstName+'&lastName='+this.props.player.lastName;
		const dialogHref = '#replacePlayer'+param; /*'#targetName'*/
		const dialogId = 'replacePlayer'+param;
		return (
			<span> {/*Use <span> so it aligns inline with others.*/}
			    <a href= {dialogHref} className = 'buttonBlue'>Update</a> {/*The hyperlink directing to the dialog.*/}
				<div id= {dialogId} className='modalDialog'> {/*The hidden dialog*/}
					<div>
						<h2>Update player</h2>
						<form>
							{inputs}
							<a href='#' className='buttonGray' onClick={this.handleCancel}>cancel</a>
							&emsp; &emsp; &emsp; &ensp; &ensp;
							<button className='buttonGreen' onClick={this.handleSubmit}>submit</button>
						</form>
					</div>
				</div>
			</span>
		)
	}
}
/*UpdatePlayer Component: end ===============================================================*/
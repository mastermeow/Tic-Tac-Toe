import React from 'react';
import {BrowserRouter, Switch, Route, Link, useRouteMatch, useParams} from 'react-router-dom';
import './app.css';
import TicTacToe from './tictactoe';
import Players from './players';

export default class App extends React.Component {
    render(){
        return (
            <BrowserRouter>
                <div className = 'app'>
                    <header className = 'app-header'>
                        <nav className = 'app-nav'>
                            <ul>
                                <li>
                                    <Link to='/'>Home</Link>
                                </li>
                                <li>
                                    <Link to='/tictactoe'>Tic-Tac-Toe</Link>
                                </li>
                                <li>
                                    <Link to='/playerlist'>Player List</Link>
                                </li>
                                <li>
                                    <Link to='/about'>About</Link>
                                </li>
                            </ul>
                        </nav>
                        <Switch>
                            <Route path='/playerlist'>
                                <Players/>
                            </Route>
                            <Route path='/tictactoe'>
                                <TicTacToe/>
                            </Route>
                            <Route path='/about'>
                                <About/>
                            </Route>
                            <Route path='/'>
                                <Home />
                            </Route>
                        </Switch>
                    </header>
                </div>
            </BrowserRouter>
        );
    }
}

function Home(){
    return(
        <div align = 'center'>
            <div>
                <h2>Welcome To My Project :)</h2>
            </div>
            <div align = 'left' className = 'homepage-description'>
                <p>
                    This web application allows you to:
                </p>
                <p>
                    1) play an interactive Tic-Tac-Toe game,
                </p>
                <p>
                    2) check the corresponding game results via a player list, and
                </p>
                <p>
                    3) create new players / modify and delete existing players.
                </p>
            </div>
        </div>
    );
}

class About extends React.Component{
    constructor(props) {
    	super(props);
    	this.state = {
    	    email: 'w25gu@uwaterloo.ca',
    	    emailSubject: 'RE: React and Spring Data REST project',
    	    emailBody: '(Any comments on my project?)',
    	    buttonText: 'Tell me what do you think about this project :)',
    	};
    	this.handleClick = this.handleClick.bind(this);
    }

    handleClick(e){
        e.preventDefault();
        var emailLink = 'mailto:'+this.state.email+'?subject='+this.state.emailSubject+'&body='+this.state.emailBody;
        window.open(emailLink);
    }

    render(){
        return(
            <div>
                <h2>Author: Weixi Gu</h2>
                <button onClick= {this.handleClick} className = 'buttonGreen'>{this.state.buttonText}</button>
            </div>
        );
    }
}


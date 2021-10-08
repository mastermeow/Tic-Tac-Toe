import React from 'react';
import {BrowserRouter, Switch, Route, Link, useRouteMatch, useParams} from 'react-router-dom';
import './app.css';
import TicTacToe from './tictactoe';
import Players from './players';

export default class App extends React.Component {
    constructor(props) {
    	super(props);
    	this.state = {
    	    email: 'weixi.gu@uwaterloo.ca',
    	    emailSubject: 'Re: Board Game',
    	    emailBody: '(Any comments on this project?)',
    	};
    	this.handleClickEmail = this.handleClickEmail.bind(this);
    }

    handleClickEmail(e){
        e.preventDefault();
        var emailLink = 'mailto:'+this.state.email+'?subject='+this.state.emailSubject+'&body='+this.state.emailBody;
        window.open(emailLink);
    }

    render(){
        return (
            <BrowserRouter>
                <div className = 'app'>
                    <header className = 'app-header'>
                        <nav className = 'app-nav'>
                            <ul>
                                <li>
                                    <Link to='/'>Introduction</Link>
                                </li>
                                <li>
                                    <Link to='/tictactoe'>Tic-Tac-Toe</Link>
                                </li>
                                <li>
                                    <Link to='/playerlist'>Player List</Link>
                                </li>
                                <li>
                                    <a href='https://github.com/mastermeow' target='_blank'>GitHub</a>
                                </li>
                                <li>
                                    <button onClick= {this.handleClickEmail}>Email Me</button>
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
                    This is a full-stack web application built with React and Spring. It allows you to:
                </p>
                <p>
                    1) play an interactive Tic-Tac-Toe game;
                </p>
                <p>
                    2) check statistics of players in Player List;
                </p>
                <p>
                    3) create, edit, or delete player(s) in Player List.
                </p>
            </div>
        </div>
    );
}

/*
class About extends React.Component{
    constructor(props) {
    	super(props);
    	this.state = {
    	    email: 'weixi.gu@uwaterloo.ca',
    	    emailSubject: 'Re: Board Game',
    	    emailBody: '(Any comments on this project?)',
    	    emailButtonText: 'Email me :)',
    	};
    	this.handleClickEmail = this.handleClickEmail.bind(this);
    }

    handleClickEmail(e){
        e.preventDefault();
        var emailLink = 'mailto:'+this.state.email+'?subject='+this.state.emailSubject+'&body='+this.state.emailBody;
        window.open(emailLink);
    }

    render(){
        return(
            <div className = 'about'>
                <span><button onClick= {this.handleClickEmail} className = 'buttonGreen'>{this.state.emailButtonText}</button></span>
            </div>
        );
    }
}
*/


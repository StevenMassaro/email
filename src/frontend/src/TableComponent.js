import React, {Component} from 'react';
import ReactTable from "react-table";
import ReactModal from "react-modal";
import "react-table/react-table.css";
import 'semantic-ui-css/semantic.min.css';
import {Button, Grid} from "semantic-ui-react";
import {toast, ToastContainer} from "react-toastify";
import 'react-toastify/dist/ReactToastify.min.css';


class TableComponent extends Component {
    constructor() {
        super();
        this.state = {
            showReadModal: false
        };
    }

    onSubjectClick = (e, row) => {
        e.preventDefault();
        toast.dismiss();
        this.setState({
            currentEmail: row,
            showReadModal: true
        });
    };

    componentDidMount() {
        this.listMessages();
    }

    listMessages = () => {
        fetch("./message/listMessages")
            .then(res => res.json())
            .then(
                (result) => {
                    this.setState({
                        isLoaded: true,
                        emails: result
                    });
                },
                // Note: it's important to handle errors here
                // instead of a catch() block so that we don't swallow
                // exceptions from actual bugs in components.
                (error) => {
                    this.setState({
                        isLoaded: true,
                        error
                    });
                }
            );
    };

    getBodyUrl = (id) => {
        let host = window.location.host;
        let url = "";
        if (host.includes("localhost:3000")) {
            url = 'http://localhost:8080'
        } else {
            url += '.';
        }
        url += "/body?id=";
        url += id;
        return url;
    };

    closeModal = (currentEmail) => {
        this.markMessageReadIndInState(currentEmail.id, true);
        this.setState({
            showReadModal: false
        });
    };

    deleteMessage = (currentEmail) => {
        this.closeModal(currentEmail);
        this.removeMessageFromState(currentEmail.id);
        fetch("./message?id=" + currentEmail.id, {method: "DELETE"})
            .then((response) => {
                    if (!response.ok) {
                        toast.error("Failed to delete '" + currentEmail.subject + "' with error '" + response.statusText + "', readding to list.", {
                            position: toast.POSITION.BOTTOM_RIGHT
                        });
                        this.addMessageToState(currentEmail);
                        console.warn(response);
                    } else {
                        toast.success("Message '" + currentEmail.subject + "' successfully deleted.", {
                            position: toast.POSITION.BOTTOM_RIGHT
                        });
                    }
                    return response;
                }
            )
    };

    removeMessageFromState = (id) => {
        let emails = Object.assign([], this.state.emails);
        emails = emails.filter(function (email) {
            return email.id !== id;
        });
        this.setState({
            emails: emails
        });
    };

    markMessageReadIndInState = (id, readInd) => {
        let emails = Object.assign([], this.state.emails);
        let adjustedEmails = emails.map(function (email) {
            if (email.id === id) {
                email.readInd = readInd;
            }
            return email;
        });
        this.setState({
            emails: adjustedEmails
        });
    };

    addMessageToState = (message) => {
        let emails = Object.assign([], this.state.emails);
        emails.push(message);
        this.setState({
            emails: emails
        });
    };

    render() {
        const {error, isLoaded, emails, currentEmail} = this.state;

        return (
            <div>
                <ToastContainer/>
                {currentEmail &&
                <ReactModal
                    isOpen={this.state.showReadModal}
                    contentLabel={"ReadEmail"}
                    ariaHideApp={false}
                >
                    <div className={"modalContentWrapper"} style={{
                        "display": "flex",
                        "width": "100%",
                        "height": "100%",
                        "flex-direction": "column",
                        "overflow": "hidden"
                    }}>
                        <Grid>
                            <Grid.Column width={13}>
                                <b>{currentEmail.subject}</b>
                            </Grid.Column>
                            <Grid.Column width={3} style={{"text-align": "right"}}>
                                {currentEmail.dateReceived}
                            </Grid.Column>
                        </Grid>
                        <iframe src={this.getBodyUrl(currentEmail.id)}
                                style={{"flex-grow": "1", "border": "none"}}
                        />
                        <div style={{"width": "100%"}}>
                            <div style={{"float": "left", "width": "50%"}}>
                                <Button onClick={() => this.closeModal(currentEmail)}>Close</Button>
                            </div>
                            <div style={{"float": "right", "width": "50%", "text-align": "right"}}>
                                <Button negative onClick={() => this.deleteMessage(currentEmail)}>Delete</Button>
                            </div>
                        </div>
                    </div>
                </ReactModal>
                }
                <ReactTable
                    data={emails}
                    columns={[
                        {
                            Header: "Date received",
                            accessor: "dateReceived",
                            maxWidth: 175
                        },
                        {
                            Header: "Account",
                            id: "account",
                            maxWidth: 200,
                            accessor: a => a.account.username
                        },
                        {
                            Header: "From",
                            id: "from",
                            Cell: row => {
                                return (
                                    <span>{row.original.fromPersonal} <span style={row.original.fromPersonal ? {"font-style":"italic"}:{}}>{row.original.fromAddress}</span></span>
                                );
                            },
                            maxWidth: 250
                        },
                        {
                            Header: "Subject",
                            accessor: "subject",
                            Cell: row => {
                                return (
                                    <a href="#"
                                       onClick={(e) => this.onSubjectClick(e, row.original)}>
                                        {row.original.readInd ? row.value :
                                            <span style={{"font-weight": "bold"}}>{row.value}</span>}
                                    </a>
                                );

                            }
                        }
                    ]}
                    defaultPageSize={100}
                    minRows={0}
                    noDataText={isLoaded ? (error ? error : "No emails in database.") : "Loading emails..."}
                    defaultSorted={[
                        {
                            id: "dateReceived",
                            desc: true
                        }
                    ]}
                    className="-striped -highlight"
                />
                <br/>
            </div>);
    }
}

export default TableComponent;
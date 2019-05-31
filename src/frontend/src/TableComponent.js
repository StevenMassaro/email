import React, {Component} from 'react';
import ReactTable from "react-table";
import ReactModal from "react-modal";
import "react-table/react-table.css";
import 'semantic-ui-css/semantic.min.css';
import {Button, Grid} from "semantic-ui-react";
import {toast, ToastContainer} from "react-toastify";
import 'react-toastify/dist/ReactToastify.min.css';
import ModalHeaderComponent from "./ModalHeaderComponent";
import './paper-clip.svg';


class TableComponent extends Component {
    constructor() {
        super();
        this.state = {
            loadingEmails: false,
            loadedEmails: false,
            showReadModal: false
        };
    }

    onSubjectClick = (e, row) => {
        e.preventDefault();
        this.setState({
            currentEmail: row,
            showReadModal: true
        });
        this.setReadIndicator(row.id);
    };

    setReadIndicator = (id) => {
        fetch("./message/" + id + "/read", {method: "PATCH"})
            .then(() => console.log("Marked message " + id + " as read."),
                () => console.log("Failed to mark message " + id + " as read."));
    };

    componentDidMount() {
        this.listMessages();
        this.performSync();
    }

    listMessages = () => {
        this.setState({
            loadingEmails: true,
            loadedEmails: false
        });
        fetch("./message/listMessages")
            .then(res => res.json())
            .then(
                (result) => {
                    this.setState({
                        loadingEmails: false,
                        loadedEmails: true,
                        emails: result
                    });
                },
                // Note: it's important to handle errors here
                // instead of a catch() block so that we don't swallow
                // exceptions from actual bugs in components.
                (error) => {
                    this.setState({
                        loadingEmails: false,
                        loadedEmails: true,
                        error
                    });
                }
            );
    };

    performSync = () => {
        fetch("./actions/sync")
            .then(res => res.json())
            .then(
                (results) => {
                    this.setState({
                        syncResults: results
                    });

                    let insertedCount = 0;
                    let deletedCount = 0;
                    let changedReadIndCount = 0;
                    // let failedAccounts = "";'

                    results.forEach(function (result) {
                        insertedCount += result.insertedCount;
                        deletedCount += result.deletedCount;
                        changedReadIndCount += result.changedReadIndCount;
                    });

                    toast.success("Sync results: "
                        + insertedCount + " inserted; "
                        + deletedCount + " deleted; "
                        + changedReadIndCount + " changed read indicator.", {
                        position: toast.POSITION.TOP_RIGHT
                    });
                    if (insertedCount + deletedCount + changedReadIndCount > 0) {
                        this.listMessages();
                    }
                },
                (result) => {
                    this.setState({
                        syncResults: result
                    });
                    toast.error("Sync failed.");
                    console.warn(result);
                }
            )
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
                            position: toast.POSITION.TOP_RIGHT
                        });
                        this.addMessageToState(currentEmail);
                        console.warn(response);
                    } else {
                        toast.success("Message '" + currentEmail.subject + "' successfully deleted.", {
                            position: toast.POSITION.TOP_RIGHT
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

    print = (email) => {
        let w = window.open();
        w.document.write(
            '<span style="all:unset">' +
            '<b>From: </b><span>' + (email.fromPersonal ? (email.fromPersonal + " ") : "") + '&#8249;' + email.fromAddress + '&#8250;</span><br>' +
            '<b>Sent: </b>' + email.dateReceived + '<br>' +
            '<b>To: </b>' + email.account.username + '<br>' +
            '<b>Subject: </b>' + email.subject +
            '<hr/><br></span>'
        );
        w.document.write(document.getElementById('emailContent').contentWindow.document.body.innerHTML);
        w.print();
        w.close();
    };

    render() {
        const {error, loadedEmails, emails, currentEmail} = this.state;

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

                        <ModalHeaderComponent
                            email={currentEmail}
                        />



                        <iframe src={this.getBodyUrl(currentEmail.id)}
                                style={{"flex-grow": "1"}}
                                id="emailContent"
                        />
                        <div style={{"width": "100%"}}>
                            <div style={{"float": "left", "width": "50%"}}>
                                <Button onClick={() => this.closeModal(currentEmail)}>Close</Button>
                                <Button onClick={() => this.print(currentEmail)}>Print</Button>
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
                            Header: "",
                            id: "attachment",
                            Cell: row => {
                                return <span>{row.original.attachments.length > 0 ?
                                    <img width={15}
                                         height={15}
                                         src={'./paper-clip.svg'}
                                         alt={"attachment"}/>
                                    : ""}</span>;
                            },
                            maxWidth: 25
                        },
                        {
                            Header: "Subject",
                            accessor: "subject",
                            Cell: row => {
                                const originalSubject = row.value;
                                const displaySubject = originalSubject ? originalSubject : "no subject";
                                return (
                                    <a href="#"
                                       onClick={(e) => this.onSubjectClick(e, row.original)}>
                                        <span style={{
                                            "font-weight": row.original.readInd ? "normal" : "bold",
                                            "font-style": (originalSubject ? "normal" : "italic")
                                        }}>
                                            {displaySubject}
                                        </span>
                                    </a>
                                );

                            }
                        }
                    ]}
                    defaultPageSize={100}
                    minRows={0}
                    noDataText={loadedEmails ? (error ? error : "No emails in database.") : "Loading emails..."}
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
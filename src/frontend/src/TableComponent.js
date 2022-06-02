import React, {Component} from 'react';
import ReactTable from "react-table";
import ReactModal from "react-modal";
import "react-table/react-table.css";
import 'semantic-ui-css/semantic.min.css';
import {Button} from "semantic-ui-react";
import {isMobile} from "react-device-detect";
import {toast, ToastContainer} from "react-toastify";
import 'react-toastify/dist/ReactToastify.min.css';
import './TableComponent.css';
import ModalHeaderComponent from "./ModalHeaderComponent";
import './paper-clip.svg';
import {formatDate} from "./Utils";


class TableComponent extends Component {
    constructor() {
        super();
        this.state = {
            loadedEmails: false,
            showReadModal: false,
            showPasswordModal: true,
            password: ''
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
        this.hidePasswordModalIfPasswordNotNeeded();
        this.listMessages();
        document.addEventListener('keydown', this.keydownHandler);
    }

    componentWillUnmount(){
        document.removeEventListener('keydown', this.keydownHandler);
    }

    listMessages = () => {
        this.setState({
            loadedEmails: false
        });
        fetch("./message/listMessages")
            .then(res => res.json())
            .then(
                (result) => {
                    this.setState({
                        loadedEmails: true,
                        emails: result
                    });
                },
                // Note: it's important to handle errors here
                // instead of a catch() block so that we don't swallow
                // exceptions from actual bugs in components.
                (error) => {
                    this.setState({
                        loadedEmails: true,
                        error
                    });
                }
            );
    };

    performSync = () => {
        const syncToastId = toast.info("Syncing...", {
            position: toast.POSITION.TOP_RIGHT,
            autoClose: false
        })
        fetch("./actions/sync", {
            body: this.state.password,
            method: 'POST'
        })
            .then(res => res.json())
            .then(
                (results) => {
                    this.setState({
                        syncResults: results
                    });

                    let insertedCount = 0;
                    let deletedCount = 0;
                    let changedReadIndCount = 0;
                    let failedAccounts = [];
                    let partiallyFailedAccounts = [];

                    results.forEach(function (result) {
                        insertedCount += result.insertedCount;
                        deletedCount += result.deletedCount;
                        changedReadIndCount += result.changedReadIndCount;
                        if (result.execStatusEnum === "RULE_END_ACCOUNT_FAILURE") {
                            failedAccounts.push(result);
                        } else if (result.execStatusEnum === "RULE_END_MESSAGE_FAILURE") {
                            partiallyFailedAccounts.push(result);
                        }
                    });
                    toast.dismiss(syncToastId)

                    toast.info("Sync results: "
                        + insertedCount + " inserted; "
                        + deletedCount + " deleted; "
                        + changedReadIndCount + " changed read indicator.", {
                        position: toast.POSITION.TOP_RIGHT
                    });

                    failedAccounts.forEach((account) => {
                        toast.error("Failed to sync: " + account.username, {
                            position: toast.POSITION.TOP_RIGHT,
                            autoClose: false
                        });
                    });

                    partiallyFailedAccounts.forEach((account) => {
                        toast.warn("Partially failed to sync (some messages may be missing): " + account.username, {
                            position: toast.POSITION.TOP_RIGHT,
                            autoClose: false
                        })
                    })

                    if (insertedCount + deletedCount + changedReadIndCount > 0) {
                        this.listMessages();
                    }
                },
                (result) => {
                    this.setState({
                        syncResults: result
                    });
                    toast.dismiss(syncToastId);
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

    closeReadModal = (currentEmail) => {
        this.markMessageReadIndInState(currentEmail.id, true);
        this.setState({
            showReadModal: false
        });
    };

    closePasswordModal = () => {
        this.setState({
            showPasswordModal: false
        })
    }

    deleteMessage = (currentEmail) => {
        this.closeReadModal(currentEmail);
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

    hidePasswordModalIfPasswordNotNeeded = () => {
        fetch("./actions/requiresPassword")
            .then((response) => {
                    if (response.ok) {
                        response.text().then((text) => {
                            if (text === "false") {
                                this.closePasswordModal();
                                this.performSync();
                            }
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
            '<b>Sent: </b>' + formatDate(new Date(email.dateReceived)) + '<br>' +
            '<b>To: </b>' + email.account.username + '<br>' +
            '<b>Subject: </b>' + email.subject +
            '<hr/><br></span>'
        );
        w.document.write(document.getElementById('emailContent').contentWindow.document.body.innerHTML);
        w.print();
        w.close();
    };

    filterRow = (filter, row) => {
        let comparisonValue;
        if (filter.id === 'from') {
            comparisonValue = row._original.fromPersonal + ' ' + row._original.fromAddress;
        } else {
            comparisonValue = row[filter.id]
        }
        return String(comparisonValue).toLowerCase().includes(String(filter.value).toLowerCase());
    };

    handlePasswordFieldChange = event => {
        this.setState({password: event.target.value});
    };

    handlePasswordFormSubmit = event => {
        this.closePasswordModal();
        this.performSync();
        event.preventDefault();
    };

    keydownHandler = e => {
        if (e.key === 's' && e.ctrlKey) {
            e.preventDefault();
            e.stopPropagation();
            this.performSync();
        }
    };

    render() {
        const {error, loadedEmails, emails, currentEmail} = this.state;

        let columns = [
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
                        <span>{row.original.fromPersonal} <span
                            style={row.original.fromPersonal ? {"fontStyle": "italic"} : {}}>{row.original.fromAddress}</span></span>
                    );
                },
                maxWidth: 250
            },
            {
                Header: "",
                id: "attachment",
                filterable: false,
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
                        <button
                            type="button"
                            className="link-button"
                            onClick={(e) => this.onSubjectClick(e, row.original)}
                        >
                            <span style={{
                                "fontWeight": row.original.readInd ? "normal" : "bold",
                                "fontStyle": (originalSubject ? "normal" : "italic")
                            }}>
                                {displaySubject}
                            </span>
                        </button>
                    );
                }
            }
        ];

        if (!isMobile) {
            columns.unshift({
                Header: "Date received",
                accessor: "dateReceived",
                Cell: row => {
                    const date = new Date(row.value);
                    // fr-CA is yyyy-MM-dd, see https://stackoverflow.com/questions/27939773/tolocaledatestring-short-format for more info
                    return (<div title={row.original.originalDateReceived}>{formatDate(date)}</div>);
                },
                maxWidth: 148
            });
        }

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
                        "flexDirection": "column",
                        "overflow": "hidden"
                    }}>

                        <ModalHeaderComponent
                            email={currentEmail}
                        />

                        <iframe src={this.getBodyUrl(currentEmail.id)}
                                style={{"flexGrow": "1"}}
                                id="emailContent"
                                title="email contents"
                        />
                        <div style={{"width": "100%"}}>
                            <div style={{"float": "left", "width": "50%"}}>
                                <Button onClick={() => this.closeReadModal(currentEmail)}>Close</Button>
                                <Button onClick={() => this.print(currentEmail)}>Print</Button>
                            </div>
                            <div style={{"float": "right", "width": "50%", "textAlign": "right"}}>
                                <Button negative onClick={() => this.deleteMessage(currentEmail)}>Delete</Button>
                            </div>
                        </div>
                    </div>
                </ReactModal>
                }
                <ReactModal
                    isOpen={this.state.showPasswordModal}
                    contentLabel={"EnterPassword"}
                    ariaHideApp={false}
                >
                    <div className={"modalContentWrapper"} style={{
                        "display": "flex",
                        "width": "100%",
                        "height": "100%",
                        "flexDirection": "column",
                        "overflow": "hidden"
                    }}>
                        <form onSubmit={this.handlePasswordFormSubmit}>
                            <label>Bitwarden Master Password: <input type="password" value={this.state.password} onChange={this.handlePasswordFieldChange} /></label>
                            <input type="submit" value="Submit" />
                        </form>
                    </div>
                </ReactModal>
                <ReactTable
                    data={emails}
                    columns={columns}
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
                    filterable={true}
                    defaultFilterMethod={this.filterRow}
                />
            </div>);
    }
}

export default TableComponent;
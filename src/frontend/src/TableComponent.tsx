import {Component} from "react";
import * as React from 'react';
import ReactTable from "react-table";
import selectTableHOC from "react-table/lib/hoc/selectTable";
import ReactModal from "react-modal";
import "react-table/react-table.css";
import {Button, Form, Grid} from "semantic-ui-react";
import {isMobile} from "react-device-detect";
import {toast, ToastContainer} from "react-toastify";
import 'react-toastify/dist/ReactToastify.min.css';
import './TableComponent.css';
import ModalHeaderComponent from "./ModalHeaderComponent";
import {formatDate} from "./Utils";
import {Email} from "./model/Email";
import * as lodash from "lodash";

const SelectTable = selectTableHOC(ReactTable);

type props = {

}

type state = {
    showReadModal: boolean,
    showPasswordModal: boolean,
    password: string,
    currentEmail: Email
    emails: Email[],
    selectedEmails: Email[],
    autoBlur: boolean,
}

class TableComponent extends Component<props, state> {
    constructor(props: props | Readonly<props>) {
        super(props);
        this.state = {
            showReadModal: false,
            showPasswordModal: true,
            password: '',
            currentEmail: undefined,
            emails: [],
            selectedEmails: [],
            autoBlur: true
        };
    }

    onSubjectClick = (e, row) => {
        e.preventDefault();
        this.setState({
            currentEmail: row,
            showReadModal: true
        });
    };

    componentDidMount() {
        this.hidePasswordModalIfPasswordNotNeeded();
        this.listMessages();
        document.addEventListener('keydown', this.keydownHandler);
        this.runAutoBlur()
    }

    /**
     * Automatically reset focus back to parent periodically so that toasts and keydowns will still work.
     */
    runAutoBlur = () => {
        setInterval(_ => {
            if (document.activeElement.tagName === "IFRAME" && this.state.showReadModal && this.state.autoBlur) {
                // @ts-ignore
                document.activeElement.blur();
            }
        }, 2000);
    }

    componentWillUnmount(){
        document.removeEventListener('keydown', this.keydownHandler);
    }

    listMessages = () => {
        fetch("./message")
            .then(res => res.json())
            .then(
                (result) => {
                    this.setState({
                        emails: result
                    });
                }
            );
    };

    performSync = () => {
        if (process.env.NODE_ENV === "development") {
            this._confirmationCallback(() => this._performSync())
        } else {
            this._performSync()
        }
    };

    _performSync = () => {
        const syncToastId = toast.info(this.buildSyncStatusToastMessage(0, null), {
            autoClose: false
        })
        fetch("./actions/sync", {
            body: this.state.password,
            method: 'POST'
        }).then(() => {
            this.syncPollStatus(syncToastId)
        });
    }

    _confirmationCallback = (callback: () => void, action: string = "sync") => {
        if (window.confirm(`Are you sure you want to ${action}?`)) {
            callback();
        }
    }

    buildSyncStatusToastMessage(completedAccountCount, totalAccountCount) {
        const innerMessage = totalAccountCount ? `/${totalAccountCount}` : ""
        return `Syncing... ${completedAccountCount}${innerMessage} accounts complete`
    }

    syncPollStatus = (syncToastId) => {
        fetch("./actions/sync/results")
            .then(res => res.json())
            .then(
                (resultWrapper) => {
                    if (!resultWrapper.complete) {
                        const completedAccountCount = resultWrapper.results.length
                        const totalAccountsCount = resultWrapper.numberOfAccounts
                        toast.update(syncToastId, {
                            render: this.buildSyncStatusToastMessage(completedAccountCount, totalAccountsCount)
                        })
                        setTimeout(() => {
                            this.syncPollStatus(syncToastId);
                        }, 2000);
                    } else {
                        let insertedCount = 0;
                        let deletedCount = 0;
                        let changedReadIndCount = 0;
                        let failedAccounts = [];
                        let partiallyFailedAccounts = [];

                        let results = resultWrapper.results;

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
                            + changedReadIndCount + " changed read indicator.");

                        failedAccounts.forEach((account) => {
                            toast.error("Failed to sync: " + account.username, {
                                autoClose: false
                            });
                        });

                        partiallyFailedAccounts.forEach((account) => {
                            toast.warn("Partially failed to sync (some messages may be missing): " + account.username, {
                                autoClose: false
                            })
                        })

                        if (insertedCount + deletedCount + changedReadIndCount > 0) {
                            this.listMessages();
                        }
                    }
                },
                (result) => {
                    toast.dismiss(syncToastId);
                    toast.error("Sync failed.");
                    console.warn(result);
                }
            )
    }

    getBodyUrl = (id: number) => {
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

    closeReadModal = (currentEmail: Email) => {
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

    deleteMessage = (currentEmail: Email) => {
        this.closeReadModal(currentEmail);
        this.removeMessageFromState(currentEmail.id);
        fetch("./message?id=" + currentEmail.id, {method: "DELETE"})
            .then((response) => {
                    if (!response.ok) {
                        toast.error("Failed to delete '" + currentEmail.subject + "' with error '" + response.statusText + "', readding to list.");
                        this.addMessageToState(currentEmail);
                        console.warn(response);
                    } else {
                        console.log("Message '" + currentEmail.subject + "' successfully deleted.");
                    }
                    return response;
                }
            )
    };

    archiveMessage = (currentEmail: Email) => {
        if (window.confirm("Archive this email?")) {
            this.closeReadModal(currentEmail);
            this.removeMessageFromState(currentEmail.id);
            return fetch("./message/archive?id=" + currentEmail.id, {method: "POST"})
                .then((response) => {
                        if (!response.ok) {
                            toast.error("Failed to archive '" + currentEmail.subject + "' with error '" + response.statusText + "', readding to list.");
                            this.addMessageToState(currentEmail);
                            console.warn(response);
                        } else {
                            toast.success("Message '" + currentEmail.subject + "' successfully archived.");
                        }
                        return response;
                    }
                )
        }
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

    removeMessageFromState = (id: number) => {
        let emails = Object.assign([], this.state.emails);
        emails = emails.filter(function (email) {
            return email.id !== id;
        });
        this.setState({
            emails: emails
        });
    };

    markMessageReadIndInState = (id: number, readInd: boolean) => {
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

    addMessageToState = (message: Email) => {
        let emails = Object.assign([], this.state.emails);
        emails.push(message);
        this.setState({
            emails: emails
        });
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

    keydownHandler = async e => {
        if (e.key === 's' && e.ctrlKey) {
            e.preventDefault();
            e.stopPropagation();
            this.performSync();
        }
        const deleteKeyCode = 46;
        const escapeKeyCode = 27;
        if (this.state.currentEmail && this.state.showReadModal) {
            if (e.keyCode === deleteKeyCode) {
                e.preventDefault();
                e.stopPropagation();
                this.deleteMessage(this.state.currentEmail);
            } else if (e.keyCode === escapeKeyCode) {
                e.preventDefault();
                e.stopPropagation();
                this.closeReadModal(this.state.currentEmail);
            }
        }
        if (!lodash.isEmpty(this.state.selectedEmails)) {
            if (e.keyCode === deleteKeyCode) {
                e.preventDefault();
                e.stopPropagation();
                for (const selectedEmail of this.state.selectedEmails) {
                    console.log("deleting " + selectedEmail.id)
                    this.deleteMessage(selectedEmail);
                    console.log("deleted " + selectedEmail.id)
                }
                this.setState({
                    selectedEmails: [],
                })
            }
        }
    };


    /**
     * returns true if the key passed is selected otherwise it should return false
     */
    isSelected = (key: string) => {
        return !lodash.isUndefined(this.state.selectedEmails.find((email) => {
            return !lodash.isUndefined(email) && email.id === lodash.toInteger(key);
        }))
    };

    /**
     * called when the use clicks a specific checkbox/radio in a row
     */
    toggleSelection = (key: string) => {
        const splits = key.split("-");
        const id = lodash.toInteger(splits[1]);
        if (this.isSelected(id.toString())) { // todo the key is coming in with some additional data, so we need to split it here or something
            this.setState((state) => ({
                selectedEmails: state.selectedEmails.filter((email) => {
                    return email.id !== id;
                })
            }));
        } else {
            this.setState((state) => ({
                selectedEmails: [...state.selectedEmails, state.emails.find((email) => {
                    return email.id === id
                })]
            }));
        }
    };


    render() {
        const {emails, currentEmail} = this.state;

        let columns = [
            {
                Header: "Account",
                id: "account",
                maxWidth: 200,
                Cell: row => {
                    return (
                        <span title={row.original.recipients ? row.original.recipients.join(", ") : ""}>
                            {row.original.username}
                        </span>
                    )
                },
                accessor: "username"
            },
            {
                Header: "From",
                id: "from",
                Cell: row => {
                    return (
                        <span title={row.original.fromPersonal + " " + row.original.fromAddress}>{row.original.fromPersonal} <span
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
                id: "dateReceived",
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
                <ToastContainer
                    position="bottom-right"
                />
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
                            closeReadModal={this.closeReadModal}
                            deleteMessage={this.deleteMessage}
                            archiveMessage={this.archiveMessage}
                            toggleAutoBlur={() => {
                                this.setState((state) => ({
                                    autoBlur: !state.autoBlur
                                }));
                            }}
                            autoBlur={this.state.autoBlur}
                        />

                        <iframe src={this.getBodyUrl(currentEmail.id)}
                                style={{"flexGrow": "1"}}
                                id="emailContent"
                                title="email contents"
                        />
                    </div>
                </ReactModal>
                }
                {this.state.showPasswordModal && <ReactModal
                    isOpen={this.state.showPasswordModal}
                    contentLabel={"EnterPassword"}
                    ariaHideApp={false}
                >
                    <Grid centered>
                        <Grid.Column width={4}>
                            <Form>
                                <Form.Field>
                                    <label>Bitwarden Master Password</label>
                                    <input autoFocus type="password" value={this.state.password} onChange={this.handlePasswordFieldChange} />
                                </Form.Field>
                                <Button type='submit' onClick={this.handlePasswordFormSubmit}>Submit</Button>
                            </Form>
                        </Grid.Column>
                    </Grid>
                </ReactModal>}
                {!this.state.showPasswordModal &&
                <SelectTable
                    keyField="id"
                    // for some reason we need to define this method this way or it won't see the state
                    isSelected={(key => this.isSelected(key))}
                    toggleSelection={this.toggleSelection}
                    selectType={"checkbox"}
                    data={emails}
                    columns={columns}
                    defaultPageSize={100}
                    minRows={0}
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
                }
            </div>);
    }
}

export default TableComponent;

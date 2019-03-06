import React, {Component} from 'react';
import ReactModal from "./TableComponent";
import {Button} from "semantic-ui-react";
import ModalHeaderComponent from "./ModalHeaderComponent";

class ModalComponent extends Component {

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
        const currentEmail = this.props.currentEmail;

        return <ReactModal
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
                        style={{"flex-grow": "1", "border": "none"}}
                        id="emailContent"
                />
                <div style={{"width": "100%"}}>
                    <div style={{"float": "left", "width": "50%"}}>
                        <Button onClick={() => this.props.modalCloseCallback(currentEmail)}>Close</Button>
                        <Button onClick={() => this.print(currentEmail)}>Print</Button>
                    </div>
                    <div style={{"float": "right", "width": "50%", "text-align": "right"}}>
                        <Button negative onClick={() => this.deleteMessage(currentEmail)}>Delete</Button>
                    </div>
                </div>
            </div>
        </ReactModal>
    }
}

export default ModalComponent;
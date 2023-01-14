import React, {Component} from 'react';
import './ModalHeaderComponent.css';
import download from 'downloadjs';
import {formatDate} from "./Utils";
import {Button} from "semantic-ui-react";

class ModalHeaderComponent extends Component {

    fetchAttachment = (attachment) => {
        fetch("./attachment?id=" + attachment.id)
            .then(function (resp) {
                return resp.blob();
            }).then(function (blob) {
            download(blob, attachment.name, attachment.contentType);
        });
    };

    print = (email) => {
        let w = window.open();
        w.document.write(
            '<span style="all:unset">' +
            '<b>From: </b><span>' + (email.fromPersonal ? (email.fromPersonal + " ") : "") + '&#8249;' + email.fromAddress + '&#8250;</span><br>' +
            '<b>Sent: </b>' + formatDate(new Date(email.dateReceived)) + '<br>' +
            '<b>To: </b>' + email.username + '<br>' +
            '<b>Subject: </b>' + email.subject +
            '<hr/><br></span>'
        );
        // @ts-ignore
        w.document.write(document.getElementById('emailContent').innerHTML);
        w.print();
        w.close();
    };

    render() {
        const currentEmail = this.props.email;
        const attachments = currentEmail.attachments;

        return <div>
            <div className="flex-container">
                <Button onClick={() => this.props.closeReadModal(currentEmail)}>Close</Button>
                <Button onClick={() => this.print(currentEmail)}>Print</Button>
                <div className="subject bold">{currentEmail.subject}</div>
                <div className="dateReceived">{formatDate(new Date(currentEmail.dateReceived))}</div>
                <Button negative onClick={() => this.props.deleteMessage(currentEmail)}>Delete</Button>
            </div>
            {attachments &&
            <div className="flex-container attachments">
                {attachments.map((attachment) => {
                    return <div key={attachment.id}>
                        <button
                            type="button"
                            className="link-button"
                            onClick={() => this.fetchAttachment(attachment)}
                        >
                            {attachment.name}
                        </button>
                    </div>;
                })
                }
            </div>
            }
        </div>
    }
}

export default ModalHeaderComponent;
import React, {Component} from 'react';
import download from 'downloadjs';
import {formatDate} from "./Utils";
import {Dropdown, Menu} from "semantic-ui-react";
import * as lodash from "lodash";

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
        w.document.title = email.subject || "Email"; // Set title to influence filename
        w.document.write(
            '<span style="all:unset">' +
            '<b>From: </b><span>' + (email.fromPersonal ? (email.fromPersonal + " ") : "") + '&#8249;' + email.fromAddress + '&#8250;</span><br>' +
            '<b>Sent: </b>' + formatDate(new Date(email.dateReceived)) + '<br>' +
            '<b>To: </b>' + email.username + '<br>' +
            '<b>Subject: </b>' + email.subject +
            '<hr/><br></span>'
        );
        // @ts-ignore
        w.document.write(document.getElementById('emailContent').contentWindow.document.body.innerHTML);
        w.print();
        w.close();
    };

    render() {
        const currentEmail = this.props.email;
        const attachments = currentEmail.attachments;

        return <div>
            <Menu>
                <Menu.Item onClick={() => this.props.closeReadModal(currentEmail)}>Close</Menu.Item>
                <Menu.Item onClick={() => this.print(currentEmail)}>Print</Menu.Item>
                {!lodash.isEmpty(attachments) && <Dropdown item text='Attachments'>
                    <Dropdown.Menu>
                        {attachments.map((attachment) => {
                            return <Dropdown.Item onClick={() => this.fetchAttachment(attachment)}>{attachment.name}</Dropdown.Item>
                        })}
                    </Dropdown.Menu>
                </Dropdown>}
                <Menu.Item><p><b>{currentEmail.subject}</b></p></Menu.Item>
                <Menu.Item><b>{formatDate(new Date(currentEmail.dateReceived))}</b></Menu.Item>
                <Menu.Menu position={"right"}>
                    <Menu.Item onClick={() => this.props.toggleAutoBlur()}>{this.props.autoBlur ? "Disable" : "Enable"} auto-blur</Menu.Item>
                    {currentEmail.username.includes("gmail") && <Menu.Item color={"red"} onClick={() => this.props.archiveMessage(currentEmail)}>Archive</Menu.Item>}
                    <Menu.Item color={"red"} onClick={() => this.props.deleteMessage(currentEmail)}>Delete</Menu.Item>
                </Menu.Menu>
            </Menu>
        </div>
    }
}

export default ModalHeaderComponent;
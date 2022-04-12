import React, {Component} from 'react';
import './ModalHeaderComponent.css';
import download from 'downloadjs';
import {formatDate} from "./Utils";

class ModalHeaderComponent extends Component {

    fetchAttachment = (attachment) => {
        fetch("./attachment?id=" + attachment.id)
            .then(function (resp) {
                return resp.blob();
            }).then(function (blob) {
            download(blob, attachment.name, attachment.contentType);
        });
    };

    render() {
        const currentEmail = this.props.email;
        const attachments = currentEmail.attachments;

        return <div>
            <div className="flex-container">
                <div className="subject bold">{currentEmail.subject}</div>
                <div className="dateReceived">{formatDate(new Date(currentEmail.dateReceived))}</div>
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
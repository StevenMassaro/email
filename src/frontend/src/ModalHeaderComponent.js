import React, {Component} from 'react';
import './ModalHeaderComponent.css';
import download from 'downloadjs';

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
                <div className="dateReceived">{currentEmail.dateReceived}</div>
            </div>
            {attachments &&
            <div className="flex-container attachments">
                {attachments.map((attachment) => {
                    return <div><a href={'#'} onClick={() => this.fetchAttachment(attachment)}>{attachment.name}</a>
                    </div>;
                })
                }
            </div>
            }
        </div>
    }
}

export default ModalHeaderComponent;
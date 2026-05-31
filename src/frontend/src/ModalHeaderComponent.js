import React, {Component} from 'react';
import download from 'downloadjs';
import {formatDate} from "./Utils";
import {Dropdown, Menu, Icon} from "semantic-ui-react";
import {isMobile} from "react-device-detect";
import * as lodash from "lodash";

class ModalHeaderComponent extends Component {
	state = {downloadingAttachmentId: null, dropdownOpen: false};

	componentDidUpdate(prevProps, prevState) {
		if (this.state.downloadingAttachmentId && !this.state.dropdownOpen) {
			this.setState({dropdownOpen: true});
		}
	}

	fetchAttachment = (attachment) => {
		this.setState({downloadingAttachmentId: attachment.id, dropdownOpen: true});
		fetch("./attachment?id=" + attachment.id)
			.then(function (resp) {
				return resp.blob();
			}).then((blob) => {
				download(blob, attachment.name, attachment.contentType);
			}).finally(() => {
				this.setState({downloadingAttachmentId: null, dropdownOpen: false});
			});
	};

	print = (email) => {
		let w = window.open();
		w.document.title = email.subject || "Email";
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
		const {downloadingAttachmentId, dropdownOpen} = this.state;
		const isMobileView = isMobile;

		return <div>
			<Menu>
				<Menu.Item onClick={() => this.props.closeReadModal(currentEmail)}>
					<Icon name="close" />
				</Menu.Item>
				<Menu.Item onClick={() => this.print(currentEmail)}>
					<Icon name="print" />
				</Menu.Item>
				{!isMobileView && <Menu.Item><b>{formatDate(new Date(currentEmail.dateReceived))}</b></Menu.Item>}
				{!lodash.isEmpty(attachments) && <Dropdown item text='Attachments' open={dropdownOpen} closeOnChange={false} onOpen={() => this.setState({dropdownOpen: true})} onClose={() => this.setState({dropdownOpen: false})}>
					<Dropdown.Menu>
						{attachments.map((attachment) => {
							const isDownloading = downloadingAttachmentId === attachment.id;
							return <Dropdown.Item
								onClick={() => this.fetchAttachment(attachment)}
								disabled={isDownloading}>
								{isDownloading ? 'Downloading...' : attachment.name}
							</Dropdown.Item>
						})}
					</Dropdown.Menu>
				</Dropdown>}
				{!isMobileView && <Menu.Item className="subject-item"><p><b>{currentEmail.subject}</b></p></Menu.Item>}
				<Menu.Menu position={"right"}>
					<Menu.Item onClick={() => this.props.openBudgetModal()} title="Add to Budget" style={{fontSize: '1.3em', fontWeight: 'bold'}}>$</Menu.Item>
					<Menu.Item onClick={() => this.props.toggleAutoBlur()} title={this.props.autoBlur ? "Disable Auto-Blur" : "Enable Auto-Blur"}>
						<Icon name={this.props.autoBlur ? "eye slash" : "eye"} />
					</Menu.Item>
					{currentEmail.username.includes("gmail") && (
						<Menu.Item color={"red"} onClick={() => this.props.archiveMessage(currentEmail)} title="Archive Message">
							<Icon name="archive" />
						</Menu.Item>
					)}
					<Menu.Item color={"red"} onClick={() => this.props.deleteMessage(currentEmail)} title="Delete Message">
						<Icon name="trash" />
					</Menu.Item>
				</Menu.Menu>
			</Menu>
		</div>
	}
}

export default ModalHeaderComponent;
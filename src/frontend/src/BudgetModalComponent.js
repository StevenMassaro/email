import React, {Component} from 'react';
import ReactModal from 'react-modal';
import {Button, Form, Label, Message} from 'semantic-ui-react';
import {toast} from 'react-toastify';

class BudgetModalComponent extends Component {
	state = {
		accounts: [],
		categoryGroups: [],
		payees: [],
		amounts: this.props.amounts || [],
		selectedAccountId: localStorage.getItem('budget_last_account') || '',
		date: '',
		amount: '',
		payeeName: '',
		selectedCategoryId: localStorage.getItem('budget_last_category') || '',
		notes: '',
		loading: false,
		loadError: null,
	};

	componentDidMount() {
		this.prefillFromEmail();
		this.fetchBudgetData();
	}

	prefillFromEmail() {
		const {email} = this.props;
		if (email) {
			const d = new Date(email.dateReceived);
			const dateStr = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
			this.setState({date: dateStr});

			const payeeName = email.fromPersonal || email.fromAddress || '';
			this.setState({payeeName});

			this.setState({notes: email.subject || ''});

			const {amounts} = this.props;
			if (amounts && amounts.length > 0) {
				const firstAmount = (amounts[0].amount / 100).toFixed(2);
				this.setState({amount: firstAmount});
			}
		}
	}

	fetchBudgetData = () => {
		const fetchSafe = (url) => fetch(url).then(r => {
			if (!r.ok) throw new Error(r.status + ' ' + r.statusText);
			return r.json();
		}).catch(err => {
			console.error('Failed to fetch ' + url, err);
			return null;
		});

		Promise.all([
			fetchSafe('./budget/accounts'),
			fetchSafe('./budget/category-groups'),
			fetchSafe('./budget/payees'),
		]).then(([accounts, categoryGroups, payees]) => {
			const hasError = accounts === null || categoryGroups === null || payees === null;
			this.setState({
				accounts: accounts || [],
				categoryGroups: categoryGroups || [],
				payees: payees || [],
				loadError: hasError ? 'Some budget data failed to load. Check that Actual Budget API is running.' : null,
			});
		});
	};

	handleAccountChange = (e, {value}) => this.setState({selectedAccountId: value});
	handleDateChange = (e, {value}) => this.setState({date: value});
	handleAmountChange = (e, {value}) => this.setState({amount: value});
	handlePayeeChange = (e, {value}) => this.setState({payeeName: value});
	handleCategoryChange = (e, {value}) => this.setState({selectedCategoryId: value});
	handleNotesChange = (e, {value}) => this.setState({notes: value});

	handleAddPayee = (e, {value}) => {
		this.setState({payeeName: value});
	};

	handleAmountChipClick = (amountCents) => {
		this.setState({amount: (amountCents / 100).toFixed(2)});
	};

	submitTransaction = () => {
		const {selectedAccountId, date, amount, payeeName, selectedCategoryId, notes} = this.state;

		if (!selectedAccountId || !date || !amount) {
			toast.error('Account, date, and amount are required');
			return Promise.reject();
		}

		this.setState({loading: true});

		const amountCents = -Math.round(parseFloat(amount) * 100);

		return fetch('./budget/transactions', {
			method: 'POST',
			headers: {'Content-Type': 'application/json'},
			body: JSON.stringify({
				accountId: selectedAccountId,
				date: date,
				amount: amountCents,
				payeeName: payeeName,
				categoryId: selectedCategoryId || null,
				notes: notes,
			}),
		}).then(response => {
			if (!response.ok) {
				return response.text().then(text => {
					throw new Error(text);
				});
			}
			return response.json();
		}).then(() => {
			localStorage.setItem('budget_last_account', selectedAccountId);
			localStorage.setItem('budget_last_category', selectedCategoryId);
			toast.success('Transaction added to budget');
		}).catch(err => {
			toast.error('Failed to add transaction: ' + err.message);
			throw err;
		}).finally(() => {
			this.setState({loading: false});
		});
	};

	handleSubmit = () => {
		this.submitTransaction().then(() => {
			this.props.onClose();
		}).catch(() => {});
	};

	handleSubmitAndDelete = () => {
		this.submitTransaction().then(() => {
			this.props.deleteMessage(this.props.email);
		}).catch(() => {});
	};

	buildCategoryOptions() {
		const {categoryGroups} = this.state;
		const options = [];
		for (const group of categoryGroups) {
			if (group.is_income || group.hidden) continue;
			const categoryOptions = (group.categories || [])
				.filter(c => !c.hidden && !c.is_income)
				.map(c => ({
					key: c.id,
					value: c.id,
					text: c.name,
				}));
			if (categoryOptions.length > 0) {
				options.push({
					key: group.id,
					label: group.name,
					options: categoryOptions,
				});
			}
		}
		for (const group of categoryGroups) {
			if (group.is_income && !group.hidden) {
				const incomeOptions = (group.categories || [])
					.filter(c => !c.hidden)
					.map(c => ({
						key: c.id,
						value: c.id,
						text: c.name,
					}));
				if (incomeOptions.length > 0) {
					options.push({
						key: group.id,
						label: group.name,
						options: incomeOptions,
					});
				}
			}
		}
		return options;
	}

	render() {
		const {accounts, payees, amounts, selectedAccountId, date, amount, payeeName, selectedCategoryId, notes, loading, loadError} = this.state;

		const accountOptions = accounts
			.filter(a => !a.closed)
			.map(a => ({
				key: a.id,
				value: a.id,
				text: a.name + (a.offbudget ? ' (off-budget)' : ''),
			}));

		const payeeOptions = payees.map(p => ({
			key: p.id,
			value: p.name,
			text: p.name,
		}));

		const categoryOptions = this.buildCategoryOptions();

		return (
			<ReactModal
				isOpen={true}
				contentLabel="Add to Budget"
				ariaHideApp={false}
				className="ReactModal__Content ReactModal__Content--budget-modal"
				style={{
					overlay: {
						zIndex: 1000,
						backgroundColor: 'rgba(0, 0, 0, 0.5)',
					},
				}}
			>
				<h3 style={{marginTop: 0}}>Add to Budget</h3>
				{loadError && <Message warning>{loadError}</Message>}
				<Form>
					<Form.Select
						label="Account"
						options={accountOptions}
						value={selectedAccountId}
						onChange={this.handleAccountChange}
						placeholder="Select account"
						search
					/>
					<Form.Input
						label="Date"
						type="date"
						value={date}
						onChange={this.handleDateChange}
					/>
					<Form.Input
						label="Amount"
						type="number"
						step="0.01"
						value={amount}
						onChange={this.handleAmountChange}
						placeholder="0.00"
					/>
					{amounts.length > 1 && (
						<div style={{marginTop: '-10px', marginBottom: '10px'}}>
							<Label.Group size="mini">
								{amounts.map((a, i) => (
									<Label
										key={i}
										as="a"
										onClick={() => this.handleAmountChipClick(a.amount)}
										color={amount === (a.amount / 100).toFixed(2) ? 'blue' : undefined}
									>
										{a.formatted}
									</Label>
								))}
							</Label.Group>
						</div>
					)}
					<Form.Select
						label="Payee"
						options={payeeOptions}
						value={payeeName}
						onChange={this.handlePayeeChange}
						onAddItem={this.handleAddPayee}
						allowAdditions
						search
						selection
						additionLabel="Create: "
					/>
					<Form.Select
						label="Category"
						options={categoryOptions}
						value={selectedCategoryId}
						onChange={this.handleCategoryChange}
						placeholder="Select category"
						search
					/>
					<Form.TextArea
						label="Notes"
						value={notes}
						onChange={this.handleNotesChange}
						rows={2}
					/>
					<div style={{display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px'}}>
						<Button onClick={this.props.onClose}>Cancel</Button>
						<Button primary onClick={this.handleSubmit} loading={loading} disabled={loading}>Add Transaction</Button>
						<Button color="red" onClick={this.handleSubmitAndDelete} loading={loading} disabled={loading}>Add Transaction & Delete Email</Button>
					</div>
				</Form>
			</ReactModal>
		);
	}
}

export default BudgetModalComponent;

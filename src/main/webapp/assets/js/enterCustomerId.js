document.addEventListener('DOMContentLoaded', () => {
	const MAX_DIGITS = 6;

	let currentInput = '';
	let isSubmitted = false;
	const display = document.getElementById('displayCustomerId');
	const messageDiv = document.getElementById('message');
	const form = document.getElementById('customerForm');
	const customerIdInput = document.getElementById('customerId');

	function isValidInput(input) {
		if (!input) return false;
		const count = parseInt(input, 10);
		return !isNaN(count) && count > 0 && count <= 999999;
	}

	function updateDisplay() {
		display.textContent = currentInput;
		updateSubmitButtonState();
	}

	function updateSubmitButtonState() {
		const submitButton = document.querySelector('.action-submit');
		if (currentInput === '' || isSubmitted) {
			submitButton.classList.add('disabled');
		} else {
			submitButton.classList.remove('disabled');
		}
	}

	document.querySelectorAll('.key').forEach((key) => {
		key.addEventListener('click', () => {
			// If submitted, ignore input
			if (isSubmitted) return;

			// If modal is open, ignore input
			if (messageDiv.style.display && messageDiv.style.display !== 'none') return;

			if (key.classList.contains('action-submit')) {
				submitForm();
			} else if (key.classList.contains('action-clear')) {
				currentInput = '';
				updateDisplay();
			} else if (key.classList.contains('action-backspace')) {
				currentInput = currentInput.slice(0, -1);
				updateDisplay();
			} else if (key.dataset.value) {
				const value = key.dataset.value;
				const potentialInput = currentInput + value;

				if (potentialInput.length <= MAX_DIGITS) {
					// Prevent leading zero logic
					if (currentInput === '' && value === '0') {
						// Prevent leading zero
					} else {
						currentInput = potentialInput;
					}
					updateDisplay();
				}
			}
		});
	});

	// Initialize button state
	updateSubmitButtonState();

	async function submitForm() {
		if (!isValidInput(currentInput)) {
			showErrorMessage('有効なお客様番号を入力してください');
			return;
		}

		// Mark as submitted to ignore further input
		isSubmitted = true;
		updateSubmitButtonState();

		const customerId = parseInt(currentInput, 10);

		try {
			const response = await fetch(form.action || window.location.href, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/x-www-form-urlencoded',
				},
				body: `customerId=${encodeURIComponent(customerId)}`,
			});

			if (response.ok) {
				location.replace(location.href);
			} else if (response.status === 404) {
				showErrorMessage('お客様IDが見つかりません。再度入力してください。');
			} else if (response.status === 409) {
				showErrorMessage('この端末は既にお客様IDが入力されています', () => {
					location.replace(location.href);
				});
			} else {
				showErrorMessage('エラーが発生しました');
			}
		} catch (error) {
			console.error(error);
			showErrorMessage('通信エラーが発生しました');
		}
	}

	function showErrorMessage(text, onClose) {
		console.error(text);

		messageDiv.innerHTML = `
			<div class="error-content">
				<p class="error-text">${text}</p>
				<button id="errorCloseBtn" class="modal-close-btn" type="button">閉じる</button>
			</div>
		`;
		messageDiv.className = 'error';
		messageDiv.style.display = 'block';

		document.getElementById('errorCloseBtn').addEventListener('click', () => {
			messageDiv.style.display = 'none';
			messageDiv.innerHTML = '';

			isSubmitted = false;
			updateDisplay();
			if (onClose) {
				onClose();
			}
		});
	}
});

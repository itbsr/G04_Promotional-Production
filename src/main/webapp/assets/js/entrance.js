document.addEventListener('DOMContentLoaded', () => {
	const CUSTOMER_MAX_COUNT = 30;

	let currentInput = '';
	const display = document.getElementById('displayCount');
	const messageDiv = document.getElementById('message');
	const submitButton = document.querySelector('.key.action-submit');

	// Initialize
	updateSubmitButtonState();

	function isValidInput(input) {
		if (!input) return false;
		const count = parseInt(input, 10);
		return !isNaN(count) && count > 0 && count <= CUSTOMER_MAX_COUNT;
	}

	function updateSubmitButtonState() {
		if (isValidInput(currentInput)) {
			submitButton.classList.remove('disabled');
		} else {
			submitButton.classList.add('disabled');
		}
	}

	function updateDisplay() {
		display.textContent = currentInput;
		updateSubmitButtonState();
	}

	function showErrorMessage(text, type) {
		console.error(text);

		// Use innerHTML to include the close button
		messageDiv.innerHTML = `
			<div class="error-content">
				<p class="error-text">${text}</p>
				<button id="errorCloseBtn" class="modal-close-btn">閉じる</button>
			</div>
		`;
		messageDiv.className = type;
		messageDiv.style.display = 'block';

		// Manual close only
		document.getElementById('errorCloseBtn').addEventListener('click', () => {
			messageDiv.style.display = 'none';
			messageDiv.innerHTML = '';
		});
	}

	function showSuccessModal(decimalId) {
		messageDiv.className = '';
		messageDiv.style.display = 'block';

		messageDiv.innerHTML = `
    <div class="modal-content">
        <p class="guidance-text">番号札をお持ちになり<br>お好きなテーブルへお進みください。</p>
        <div class="customer-label">お客様番号</div>
        <div class="customer-number-large">${decimalId}</div>
        <button id="modalOkBtn" class="modal-ok-btn">OK</button>
    </div>
`;

		document.getElementById('modalOkBtn').addEventListener('click', () => {
			messageDiv.style.display = 'none';
			messageDiv.innerHTML = ''; // Clean up
			currentInput = '';
			updateDisplay();
		});
	}

	document.querySelectorAll('.key').forEach((key) => {
		key.addEventListener('click', () => {
			// If modal is open (checked by display), ignore input
			if (messageDiv.style.display && messageDiv.style.display !== 'none') return;

			if (key.classList.contains('action-submit')) {
				if (!submitButton.classList.contains('disabled')) {
					submitForm();
				}
			} else if (key.classList.contains('action-clear')) {
				currentInput = '';
				updateDisplay();
			} else if (key.classList.contains('action-backspace')) {
				currentInput = currentInput.slice(0, -1);
				updateDisplay();
			} else if (key.dataset.value) {
				const value = key.dataset.value;
				const potentialInput = currentInput + value;
				const potentialCount = parseInt(potentialInput, 10);

				if (
					potentialInput.length <= 2 &&
					!isNaN(potentialCount) &&
					potentialCount <= CUSTOMER_MAX_COUNT
				) {
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

	async function submitForm() {
		if (!isValidInput(currentInput)) {
			showErrorMessage('有効な人数を入力してください', 'error');
			return;
		}

		const count = parseInt(currentInput, 10);

		try {
			const response = await fetch('api/v1/customers', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ customerCount: count }),
			});

			if (response.ok) {
				const data = await response.json();
				// Extract ID part
				// UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
				const uuidParts = data.customerId.split('-');
				const lastPart = uuidParts[uuidParts.length - 1]; // Hex string
				const decimalId = parseInt(lastPart, 16);

				showSuccessModal(decimalId);
			} else {
				showErrorMessage('エラーが発生しました', 'error');
			}
		} catch (error) {
			console.error(error);
			showErrorMessage('通信エラーが発生しました', 'error');
		}
	}
});

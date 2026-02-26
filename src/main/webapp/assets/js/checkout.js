let customerId = '';
const MAX_VALUE = 999999;

// Modal functions
function showModal(title, message, onConfirm, isPositive = true) {
	const modal = document.getElementById('confirmModal');
	const modalTitle = document.getElementById('modalTitle');
	const modalMessage = document.getElementById('modalMessage');
	const confirmBtn = document.getElementById('modalConfirmBtn');

	modalTitle.textContent = title;
	modalMessage.textContent = message;

	// Remove previous event listeners by cloning
	const newConfirmBtn = confirmBtn.cloneNode(true);
	confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);

	// Set button style based on isPositive
	if (isPositive) {
		newConfirmBtn.classList.remove('modal-btn-danger');
		newConfirmBtn.classList.add('modal-btn-confirm');
	} else {
		newConfirmBtn.classList.remove('modal-btn-confirm');
		newConfirmBtn.classList.add('modal-btn-danger');
	}

	newConfirmBtn.addEventListener('click', function () {
		closeModal();
		if (onConfirm) onConfirm();
	});

	modal.style.display = 'flex';
	// Trigger animation
	setTimeout(() => modal.classList.add('active'), 10);
}

function closeModal() {
	const modal = document.getElementById('confirmModal');
	modal.classList.remove('active');
	setTimeout(() => (modal.style.display = 'none'), 300);
}

function showSuccessModal() {
	const modal = document.getElementById('successModal');
	modal.style.display = 'flex';
	setTimeout(() => modal.classList.add('active'), 10);
}

function closeSuccessModal() {
	const modal = document.getElementById('successModal');
	modal.classList.remove('active');
	setTimeout(() => {
		modal.style.display = 'none';
		location.replace(location.href);
	}, 300);
}

// Error modal helpers
function showErrorModal(message) {
	const modal = document.getElementById('errorModal');
	const msgEl = document.getElementById('errorModalMessage');
	if (msgEl) msgEl.textContent = message || 'エラーが発生しました。';
	modal.style.display = 'flex';
	setTimeout(() => modal.classList.add('active'), 10);
}

function closeErrorModal() {
	const modal = document.getElementById('errorModal');
	modal.classList.remove('active');
	setTimeout(() => (modal.style.display = 'none'), 300);
}

// Close modal when clicking outside
window.onclick = function (event) {
	const confirmModal = document.getElementById('confirmModal');
	const successModal = document.getElementById('successModal');
	if (event.target === confirmModal) {
		closeModal();
	} else if (event.target === successModal) {
		closeSuccessModal();
	}
};

function addDigit(digit) {
	const newValue = customerId + digit;
	if (parseInt(newValue) <= MAX_VALUE) {
		customerId = newValue;
		updateDisplay();
	}
}

function deleteDigit() {
	customerId = customerId.slice(0, -1);
	updateDisplay();
}

function clearAll() {
	customerId = '';
	updateDisplay();
}

function updateDisplay() {
	const displayValue = customerId || '——————';
	document.getElementById('customerIdDisplay').value = customerId;
	document.getElementById('customerIdInput').value = customerId;

	// Update large display on left panel
	const displayValueLarge = document.getElementById('displayValueLarge');
	if (displayValueLarge) {
		displayValueLarge.textContent = displayValue;
	}

	document.getElementById('submitBtn').disabled = customerId.length === 0;
}

function goBack() {
	showModal(
		'入力画面に戻る',
		'入力画面に戻りますか？',
		function () {
			location.replace(location.href);
		},
		false,
	); // Negative action (going back)
}

function completeCheckout(hasUnservedOrders = false) {
	let title;
	let message;
	let isPositive;

	if (hasUnservedOrders) {
		title = '⚠️ 未提供の注文があります';
		message = 'まだ提供されていない注文があります。\n本当に会計を完了しますか？';
		isPositive = false;
	} else {
		title = '会計完了の確認';
		message = '会計を完了してもよろしいですか？';
		isPositive = true;
	}

	showModal(
		title,
		message,
		function () {
			// Perform API call to mark customer as paid
			const cidEl = document.getElementById('currentCustomerId');
			if (!cidEl) {
				showErrorModal('内部エラー: 顧客IDが見つかりません。');
				return;
			}
			const cid = cidEl.value;
			fetch(location.href, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
				},
				body: new URLSearchParams({ customerId: cid, action: 'complete' }),
			})
				.then((response) => {
					if (response.ok) {
						showSuccessModal();
					} else {
						return response.text().then((text) => {
							showErrorModal(text || '会計処理に失敗しました。');
						});
					}
				})
				.catch((err) => {
					console.error('Checkout API error', err);
					showErrorModal('通信エラーが発生しました。');
				});
		},
		isPositive,
	);
}

function printReceipt() {
	window.print();
}

// Handle Enter key on form
document.addEventListener('DOMContentLoaded', function () {
	const form = document.getElementById('checkoutForm');
	if (form) {
		form.addEventListener('submit', function (e) {
			if (customerId.length === 0) {
				e.preventDefault();
			}
		});
	}
});

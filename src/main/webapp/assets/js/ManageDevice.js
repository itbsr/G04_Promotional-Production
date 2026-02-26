document.getElementById('executeDeleteBtn').addEventListener('click', () => {
	const checkboxes = document.querySelectorAll('input[name="deleteCell"]:checked');
	if (checkboxes.length === 0) {
		alert('削除するDeviceを選択してください。');
		return;
	}

	if (!confirm(`${checkboxes.length}件のDeviceを削除します。よろしいですか？`)) {
		return;
	}

	const deviceIds = Array.from(checkboxes).map((checkbox) => checkbox.value);
	const json = JSON.stringify({ deviceIds });
	const url = '../api/v1/devices';

	fetch(url, {
		method: 'DELETE',
		headers: {
			'Content-Type': 'application/json',
		},
		body: json,
	})
		.then((response) => {
			if (response.ok) {
				alert('選択したDeviceを削除しました。');
				location.replace(location.href);
			} else if (response.status === 400) {
				alert('入力内容に不備があります。');
			} else if (response.status === 500) {
				alert('サーバーエラーが発生しました。');
			} else if (response.status === 404) {
				alert('指定されたDeviceが見つかりません。');
			} else {
				alert('予期せぬエラーが発生しました。');
			}
		})
		.catch((error) => {
			alert('サーバーとの通信に失敗しました。');
		});
});

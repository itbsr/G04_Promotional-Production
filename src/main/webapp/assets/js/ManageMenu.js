// モーダルを開く
function openModal(e) {
	if (!e || !e.target) return;
	const modal = document.getElementById('addMenuModal');
	const modalTitle = document.getElementById('modalTitle');
	const submitButton = document.getElementById('modalSubmitButton');
	const form = document.getElementById('addMenuForm');

	if (e.target.classList.contains('menu-add')) {
		// 追加モード
		modalTitle.textContent = '新しいメニューを追加';
		submitButton.textContent = '登録';
		if (form) form.reset(); // フォームをリセット
		document.getElementById('menuId').value = ''; // メニューIDをクリア
		document.querySelector('.optionModal').disabled = true; // オプション選択ボタンを無効化
	} else if (e.target.classList.contains('menu-edit')) {
		const menuId = e.target.getAttribute('data-menu-id');
		const menuName = e.target.getAttribute('data-menu-name');
		const categoryMain = e.target.getAttribute('data-menu-category');
		const menuPrice = e.target.getAttribute('data-menu-price');
		const imageUrl = e.target.getAttribute('data-menu-image-url');

		// 編集モード
		modalTitle.textContent = 'メニューを編集';
		submitButton.textContent = '更新';
		document.getElementById('menuId').value = menuId || '';
		document.getElementById('menuName').value = menuName || '';
		document.getElementById('categorySubSelect').value = categoryMain || '';
		document.getElementById('menuPrice').value = menuPrice || '';
		document.querySelector('.optionModal').disabled = false; // オプション選択ボタンを有効化

		if (imageUrl) {
			const elOptionCurrentImage = document.createElement('option');
			elOptionCurrentImage.setAttribute('value', imageUrl || '');
			elOptionCurrentImage.textContent = `変更なし: ${imageUrl}`;
			elOptionCurrentImage.selected = true;

			document.getElementById('imageSelect').prepend(elOptionCurrentImage);
		}
	}

	modal.style.display = 'block'; // モーダルを表示
}

// モーダルを閉じる
function closeModal() {
	document.getElementById('addMenuModal').style.display = 'none';
	document.querySelector('form').reset();
	const combo = document.getElementById('categorySubSelect');
	if (combo) {
		combo.value = '';
	}
	location.reload();
}

// メニュー削除ボタンの処理
function deleteBtn(e) {
	if (!e || !e.target) return;

	const checkboxes = document.querySelectorAll('.delete-checkbox-cell');
	const isVisible = checkboxes[0] && checkboxes[0].style.display !== 'none';
	const controlPanel = document.getElementById('deleteControlPanel');

	// チェックボックス列の表示/非表示
	checkboxes.forEach((cell) => {
		cell.style.display = isVisible ? 'none' : 'table-cell';
	});

	// ボタンのテキストを切り替え
	e.target.textContent = isVisible ? 'メニューを削除' : 'キャンセル';

	// コントロールパネルの表示/非表示
	if (controlPanel) {
		controlPanel.style.display = isVisible ? 'none' : 'block';
	}

	if (e.target.textContent === 'キャンセル') {
		// チェックされたチェックボックスをクリア
		checkboxes.forEach((cell) => {
			const checkbox = cell.querySelector('input[type="checkbox"]');
			if (checkbox) {
				checkbox.checked = false;
			}
		});
		updateSelectedCount();
	}
}

// 選択中のチェックボックス数を更新
function updateSelectedCount() {
	const checkboxes = document.querySelectorAll('input[name="deleteMenu"]:checked');
	const countElement = document.getElementById('selectedCount');
	if (countElement) {
		countElement.textContent = checkboxes.length;
	}
}

document.getElementById('executeDeleteBtn').addEventListener('click', function () {
	const checkboxes = document.querySelectorAll('input[name="deleteMenu"]:checked');
	if (checkboxes.length != 0) {
		if (confirm(`${checkboxes.length}件のメニューを削除します。よろしいですか？`)) {
			const idsToDelete = Array.from(checkboxes).map((cb) => parseInt(cb.value));
			const json = JSON.stringify({ ids: idsToDelete });

			fetch(`../api/v1/menus`, {
				method: 'DELETE',
				headers: {
					'Content-Type': 'application/json',
				},
				body: json,
			})
				.then((response) => {
					if (response.ok) {
						return response.json();
					}

					if (response.status === 400) {
						throw new Error('入力内容に不備があります。');
					} else if (response.status === 500) {
						throw new Error('サーバーエラーが発生しました。');
					} else {
						throw new Error('削除に失敗しました。');
					}
				})
				.then((data) => {
					if (!data) {
						throw new Error('サーバーからの応答が不正です。');
					}
					const requestCnt = data.requestCount;
					const deletedCnt = data.deletedCount;
					const alreadyDeletedCnt = data.alreadyDeletedCount;
					if (requestCnt == alreadyDeletedCnt) {
						alert('選択したメニューは全て既に削除済みです。');
					} else {
						alert(
							`${requestCnt}件中${deletedCnt}件のメニューを削除しました。残りの${alreadyDeletedCnt}件は既に削除済みです。`,
						);
					}
					location.reload();
				})
				.catch((e) => {
					alert(e.message || 'メニューの削除に失敗しました。');
				});
		}
	}
});

// チェックボックスの変更を監視
document.addEventListener('change', function (e) {
	if (e.target.name === 'deleteMenu') {
		updateSelectedCount();
	}
});
document.getElementById('addMenuForm').addEventListener('submit', function (e) {
	e.preventDefault(); // フォームのデフォルト送信を防止
});
function submitForm() {
	if (!confirm('メニューを登録・更新します。よろしいですか？')) {
		return;
	}
	// Create body data
	const form = document.getElementById('addMenuForm');
	const formData = new FormData(form);
	const formObject = Object.fromEntries(formData.entries());
	// 型変換
	formObject.price = parseInt(formObject.price, 10);
	formObject.categoryId = parseInt(formObject.categoryId, 10);
	const formJson = JSON.stringify(formObject);

	// select HTTP-method and Endpoint URL
	const menuId = form.querySelector('#menuId').value;
	const method = menuId ? 'PATCH' : 'POST';
	const url = menuId ? `../api/v1/menus/${menuId}` : '../api/v1/menus';

	console.log(formJson);
	// Send request
	return fetch(url, {
		method: method,
		headers: {
			'Content-Type': 'application/json',
		},
		body: formJson,
	})
		.then((response) => {
			if (response.ok) {
				console.log('ok');
				return response.json();
			} else if (response.status == 400) {
				alert('入力内容に不備があります。');
			} else if (response.status == 404) {
				alert('指定されたメニューが見つかりません。削除されている可能性があります。');
			} else if (response.status == 500) {
				alert('サーバーエラーが発生しました。');
			} else {
				alert('メニューの登録・更新に失敗しました。');
			}
			return Promise.reject();
		})
		.catch((error) => {
			alert('サーバーとの通信に失敗しました。');
		});
}

const modalSubmitButton = document.getElementById('modalSubmitButton');
modalSubmitButton.addEventListener('click', function (e) {
	submitForm()
		.then((json) => {
			const menuId = json.id;
			if (!menuId) {
				alert('メニューIDの取得に失敗しました。');
				return;
			}
			document.querySelector('.optionModal').disabled = false; // オプション選択ボタンを有効化
			document.getElementById('menuId').value = menuId || '';

			// タイトルとボタンを更新モードに変更
			document.getElementById('modalTitle').textContent = 'メニューを編集';
			document.getElementById('modalSubmitButton').textContent = '更新';
		})
		.catch(() => {
			// エラー時は何もしない（アラートは既に表示されている）
		});
});
// オプション選択モーダルを開く
function openOptionSelectModal() {
	const optionModal = document.getElementById('optionSelectModal');
	const menuId = document.getElementById('menuId').value;
	if (!menuId) {
		alert('メニューを先に保存してください。');
		return;
	}
	fetch(`../api/v1/menus/${menuId}/options`, {
		method: 'GET',
		headers: {
			'Content-Type': 'application/json',
		},
	})
		.then((response) => {
			if (response.ok) {
				return response.json();
			} else {
				throw new Error('オプション情報の取得に失敗しました。');
			}
		})
		.then((data) => {
			renderOptionList(data);
			optionModal.style.display = 'block';
		})
		.catch((error) => {
			alert(error.message || 'オプション情報の取得に失敗しました。');
		});
}

function closeOptionSelectModal() {
	const optionModal = document.getElementById('optionSelectModal');
	optionModal.style.display = 'none';
	// フォームをリセット
	document.getElementById('newOptionName').value = '';
	document.getElementById('newOptionPrice').value = '';
}

// オプションを保存する（登録または更新）
function saveOption() {
	const menuId = document.getElementById('menuId').value;
	const editOptionId = document.getElementById('editOptionId').value;
	const optionName = document.getElementById('newOptionName').value.trim();
	const optionPrice = document.getElementById('newOptionPrice').value;

	// バリデーション
	if (!menuId) {
		alert('メニューを先に保存してください。');
		return;
	}
	if (!optionName) {
		alert('オプション名を入力してください。');
		return;
	}
	if (optionPrice === '' || optionPrice < 0) {
		alert('価格を0以上で入力してください。');
		return;
	}
	if (parseInt(optionPrice, 10) >= 100000000) {
		alert('価格は1億円未満で入力してください。');
		return;
	}

	const requestBody = {
		menuId: parseInt(menuId, 10),
		name: optionName,
		price: parseInt(optionPrice, 10),
	};

	// 編集モードか登録モードかを判定
	const isEditMode = editOptionId !== '';
	const url = isEditMode ? `../api/v1/options/${editOptionId}` : '../api/v1/options';
	const method = isEditMode ? 'PATCH' : 'POST';

	fetch(url, {
		method: method,
		headers: {
			'Content-Type': 'application/json',
		},
		body: JSON.stringify(requestBody),
	})
		.then((response) => {
			if (response.ok || response.status === 201 || response.status === 204) {
				return response.status === 204 ? {} : response.json();
			} else if (response.status === 400) {
				throw new Error('入力内容に不備があります。');
			} else if (response.status === 404) {
				throw new Error('オプションが見つかりません。');
			} else if (response.status === 500) {
				throw new Error('サーバーエラーが発生しました。');
			} else {
				throw new Error('オプションの保存に失敗しました。');
			}
		})
		.then((data) => {
			alert(isEditMode ? 'オプションを更新しました。' : 'オプションを登録しました。');
			// フォームをリセット
			resetOptionForm();
			// オプション一覧を再読み込み
			refreshOptionList();
		})
		.catch((error) => {
			alert(error.message || 'オプションの保存に失敗しました。');
		});
}

// オプションを編集モードにする
function editOption(optionId, optionName, optionPrice) {
	// フォームに値をセット
	document.getElementById('editOptionId').value = optionId;
	document.getElementById('newOptionName').value = optionName;
	document.getElementById('newOptionPrice').value = optionPrice;

	// フォームのタイトルとボタンを更新
	document.getElementById('optionFormTitle').textContent = 'オプションを編集';
	document.getElementById('registerOptionBtn').textContent = 'オプションを更新';
	document.getElementById('cancelEditOptionBtn').style.display = 'inline-block';

	// フォームにフォーカス
	document.getElementById('newOptionName').focus();
}

// オプション編集をキャンセル
function cancelEditOption() {
	resetOptionForm();
}

// オプションフォームをリセット
function resetOptionForm() {
	document.getElementById('editOptionId').value = '';
	document.getElementById('newOptionName').value = '';
	document.getElementById('newOptionPrice').value = '';
	document.getElementById('optionFormTitle').textContent = '新しいオプションを登録';
	document.getElementById('registerOptionBtn').textContent = 'オプションを登録';
	document.getElementById('cancelEditOptionBtn').style.display = 'none';
}

// オプション一覧を更新する
function refreshOptionList() {
	const menuId = document.getElementById('menuId').value;
	if (!menuId) return;

	fetch(`../api/v1/menus/${menuId}/options`, {
		method: 'GET',
		headers: {
			'Content-Type': 'application/json',
		},
	})
		.then((response) => {
			if (response.ok) {
				return response.json();
			} else {
				throw new Error('オプション情報の取得に失敗しました。');
			}
		})
		.then((data) => {
			renderOptionList(data);
		})
		.catch((error) => {
			console.error(error);
		});
}

// オプション一覧を描画する
function renderOptionList(data) {
	const optionCell = document.querySelector('.option-cell');
	if (data.length === 0) {
		optionCell.innerHTML = '<p>登録されているオプションはありません。</p>';
		return;
	}
	optionCell.innerHTML = `
		<table border="1" class="option-table">
			<tr>
				<th>オプション名</th>
				<th>価格</th>
				<th>操作</th>
			</tr>
		</table>
	`;
	const table = optionCell.querySelector('.option-table');
	data.forEach((option) => {
		const row = document.createElement('tr');
		row.innerHTML = `
			<td>${escapeHtml(option.name)}</td>
			<td>${option.price}円</td>
			<td class="option-action-buttons">
				<button type="button" class="edit-option-btn" onclick="editOption(${option.id}, '${escapeHtml(option.name).replace(/'/g, "\\'")}', ${option.price})">編集</button>
				<button type="button" class="remove-option-btn" onclick="deleteOption(${option.id})">削除</button>
			</td>
		`;
		table.appendChild(row);
	});
}

// HTMLエスケープ関数
function escapeHtml(text) {
	const div = document.createElement('div');
	div.textContent = text;
	return div.innerHTML;
}

// オプションを削除する
function deleteOption(optionId) {
	if (!confirm('このオプションを削除しますか？')) {
		return;
	}

	fetch('../api/v1/options', {
		method: 'DELETE',
		headers: {
			'Content-Type': 'application/json',
		},
		body: JSON.stringify({ optionIds: [optionId] }),
	})
		.then((response) => {
			if (response.ok) {
				return response.json();
			} else {
				throw new Error('オプションの削除に失敗しました。');
			}
		})
		.then((data) => {
			if (data.deletedCount > 0) {
				alert('オプションを削除しました。');
			} else {
				alert('オプションは既に削除されています。');
			}
			refreshOptionList();
		})
		.catch((error) => {
			alert(error.message || 'オプションの削除に失敗しました。');
		});
}

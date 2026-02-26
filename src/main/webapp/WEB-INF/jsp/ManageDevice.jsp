<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8" />
		<title>Devices</title>
		<script src="<c:url value='/assets/js/ManageDevice.js'/>" defer></script>
	</head>
	<body>
		<h1>Device登録</h1>
		<form method="post">
			<table>
				<tr>
					<td>
						<label for="deviceId">ID:</label>
					</td>
					<td>
						<input
							type="text"
							id="deviceId"
							name="deviceId"
							required
							pattern="^[0-9a-fA-F-]+$"
							title="UUIDまたは整数を入力してください"
						/>
					</td>
				</tr>
				<tr>
					<td>
						<label for="deviceName">Name:</label>
					</td>
					<td>
						<input type="text" id="deviceName" name="deviceName" required />
					</td>
				</tr>
				<tr>
					<td>
						<label for="deviceTypeId">Type:</label>
					</td>
					<td>
						<select id="deviceTypeId" name="deviceTypeId" required>
							<option value="">-- デバイスタイプを選択してください --</option>
							<c:forEach var="deviceType" items="${vm.deviceTypes}">
								<option value="${deviceType.id}">${deviceType.name}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td>
						<label for="tableId">Table:</label>
					</td>
					<td>
						<select id="tableId" name="tableId">
							<option value="">テーブル割り当てなし</option>
							<c:forEach var="table" items="${vm.tables}">
								<option value="${table.id}">${table.name} (Capacity: ${table.capacity})</option>
							</c:forEach>
						</select>
					</td>
				</tr>
			</table>
			<button type="submit">Register Device</button>
			<c:if test="${not empty message}">
				<span style="color: #f00">${message}</span>
			</c:if>
		</form>

		<h2>登録済みデバイス</h2>
		<table border="1">
			<tr>
				<th>削除</th>
				<th>ID</th>
				<th>Name</th>
				<th>Type</th>
				<th>Table</th>
			</tr>
			<c:forEach var="device" items="${vm.devices}">
				<tr>
					<td>
						<label for="delete-${device.id}">
							<input
								type="checkbox"
								name="deleteCell"
								class="delete-checkbox"
								value="${device.id}"
							/>
						</label>
					</td>
					<td>${device.id}</td>
					<td><c:out value="${device.name}"/></td>
					<td>${device.deviceTypeName}</td>
					<td>${device.tableName != null ? device.tableName : ''}</td>
				</tr>
			</c:forEach>
		</table>
		<button id="executeDeleteBtn">DeleteDevice</button>
	</body>
</html>

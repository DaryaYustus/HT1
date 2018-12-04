<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="app.Person"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Edit phone</title>
</head>
<body bgcolor="#ADD8E6">

	<%
		Person person = new Person();
		if (request.getAttribute("person") != null) {
			person = (Person) request.getAttribute("person");
		}
		String error_message = (String) request.getAttribute("error_message");
		boolean addValidator = false;
		if (request.getAttribute("add_validator") != null) {
			addValidator = Boolean.valueOf((String) request.getAttribute("add_validator"));
		}
		String phone = ((String) request.getAttribute("phone")).trim();
		if (phone != null && !phone.equals("") && !phone.startsWith("+")) {
			phone = "+" + phone;
		}
	%>


	<form action="<%=request.getContextPath()%>/" method="post">
		<input type="hidden" name="id" value="<%=person.getId()%>" /> <input
			type="hidden" name="old_phone" value="<%=phone%>" />
		<table align="center" cellspacing="2" border="3" width="70%" bgcolor="yellow">
			<%
				if ((error_message != null) && (!error_message.equals(""))) {
			%>
			<tr>
				<td colspan="2" align="center"><span style="color: red"><%=error_message%></span></td>
			</tr>
			<%
				}
			%>


			<tr>
				<td colspan="6" align="left">Информация о телефоне владельца: <%=person.getSurname() + " " + person.getName() + " " + person.getSurname()%></td>

			</tr>
			<tr>
				<td align="left"><b>Номер: </b></td>
				<td><input type="text" name="phone" value="<%=phone%>" /></td>
			</tr>
			<tr>
				<%
					if (phone == null || phone.equals("") || addValidator) {
				%>
				<td colspan="6" align="center"><input type="hidden"
					name="phone_action" value="add_phone_go" /> <input type="submit"
					name="add_phone_go" value=" Добавить номер "> <%
 	} else {
 %>
				<td colspan="6" align="center"><input type="hidden"
					name="phone_action" value="edit_phone_go" /><input type="submit"
					name="edit_phone_go" value=" Сохранить номер "> <%
 	}
 %> </br> <a
					href="<%=request.getContextPath()%>/?action=edit&id=<%=person.getId()%>">Вернуться
						к данным о человеке</a></td>
			</tr>
		</table>
	</form>

</body>
</html>
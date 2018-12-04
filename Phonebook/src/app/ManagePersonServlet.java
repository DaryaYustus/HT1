package app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ManagePersonServlet extends HttpServlet {

	// Идентификатор для сериализации/десериализации.
	private static final long serialVersionUID = 1L;

	HashMap<String, String> jsp_parameters;

	// Основной объект, хранящий данные телефонной книги.
	private Phonebook phonebook;

	private RequestDispatcher dispatcher_for_edit_phone;
	private RequestDispatcher dispatcher_for_manager;
	private RequestDispatcher dispatcher_for_list;

	public ManagePersonServlet() {
		// Вызов родительского конструктора.
		super();

		// Хранилище параметров для передачи в JSP.
		jsp_parameters = new HashMap<String, String>();
		// Диспетчеры для передачи управления на разные JSP (разные представления
		// (view)).
		// Создание экземпляра телефонной книги.
		try {
			this.phonebook = Phonebook.getInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// Реакция на GET-запросы.
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		setDefaultRequestConfiguration(request);

		// Действие (action) и идентификатор записи (id) над которой выполняется это
		// действие.
		String action = request.getParameter("action");
		String id = request.getParameter("id");
		Person person;
		String phone = request.getParameter("phone");
		// Если идентификатор и действие не указаны, мы находимся в состоянии
		// "просто показать список и больше ничего не делать".
		if ((action == null) && (id == null)) {
			actionDispatcherList(request, response);
		}
		// Если же действие указано, то...
		else {
			switch (action) {
			// Добавление записи.
			case "add":
				addPerson(request, response);
				break;
			// Редактирование записи.
			case "edit":
				editPerson(request, response, id);
				break;
			// Удаление записи.
			case "delete":
				deletePerson(request, response, id);
				break;
			case "edit_phone":
				// Извлечение из телефонной книги информации о редактируемой записи.
				person = this.phonebook.getPerson(id);
				request.setAttribute("phone", phone);
				request.setAttribute("person", person);
				dispatcher_for_edit_phone.forward(request, response);
				break;
			case "delete_phone":
				person = this.phonebook.getPerson(id);
				this.phonebook.deletePhoneNumber(id, phone);
				request.removeAttribute("phone");
				editPerson(request, response, id);
				break;
			case "add_phone":
				person = this.phonebook.getPerson(id);
				request.setAttribute("person", person);
				request.setAttribute("phone", "");
				dispatcher_for_edit_phone.forward(request, response);
				break;
			}

		}
	}

	// Реакция на POST-запросы.
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		setDefaultRequestConfiguration(request);

		// Действие (add_go, edit_go) и идентификатор записи (id) над которой
		// выполняется это действие.
		String add_go = request.getParameter("add_go");
		String edit_go = request.getParameter("edit_go");
		String phone_action = request.getParameter("phone_action");
		String id = request.getParameter("id");

		// Добавление записи.
		if (add_go != null) {
			addPersonGo(request, response);
		}

		// Редактирование записи.
		if (edit_go != null) {
			editPersonGo(request, response, id);
		}

		if (phone_action != null) {
			String phone = request.getParameter("phone");
			String old_phone = request.getParameter("old_phone");
			String error_message = processPhone(phone_action, id, phone, old_phone);
			if (error_message == null) {
				editPerson(request, response, id);
			} else {
				request.setAttribute("person", this.phonebook.getPerson(id));
				request.setAttribute("phone", phone);
				request.setAttribute("error_message", error_message);
				if (phone_action.equals("add_phone_go")) {
					request.setAttribute("add_validator", "true");
				}
				dispatcher_for_edit_phone.forward(request, response);
			}
		}
	}

	private String processPhone(String phone_action, String id, String phone, String old_phone) {
		String error_message = validatePhone(phone);
		if (error_message == null) {
			try {
				if (phone_action.equals("edit_phone_go")) {
					this.phonebook.updatePhoneNumber(id, old_phone, phone);
				} else {
					this.phonebook.addPhoneNumber(id, phone);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				error_message = "Невозможно добавить или изменить телефонный номер";
			}
		}
		return error_message;
	}

	private String validatePhone(String phone) {

		String error_message = null;

		Matcher matcher = Pattern.compile("^[+][0-9#-]{2,50}$").matcher(phone);
		if (!matcher.matches()) {
			error_message = "Телефон должен содержать от 2 до 50 символов (цифра, -, #) и начинаться с";
		}
		return error_message;
	}

	private void deletePerson(HttpServletRequest request, HttpServletResponse response, String id)
			throws ServletException, IOException {
		// Если запись удалось удалить...
		if (phonebook.deletePerson(id)) {
			jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
			jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
		}
		// Если запись не удалось удалить (например, такой записи нет)...
		else {
			jsp_parameters.put("current_action_result", "DELETION_FAILURE");
			jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
		}

		actionDispatcherList(request, response);
	}

	private void editPerson(HttpServletRequest request, HttpServletResponse response, String id)
			throws ServletException, IOException {
		// Извлечение из телефонной книги информации о редактируемой записи.
		Person editable_person = this.phonebook.getPerson(id);

		// Подготовка параметров для JSP.
		jsp_parameters.put("current_action", "edit");
		jsp_parameters.put("next_action", "edit_go");
		jsp_parameters.put("next_action_label", "Сохранить");
		jsp_parameters.put("show_phones", "true");

		// Установка параметров JSP.
		request.setAttribute("person", editable_person);
		actionDispatcherPerson(request, response);
	}

	private void addPerson(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Создание новой пустой записи о пользователе.
		Person empty_person = new Person();

		// Подготовка параметров для JSP.
		jsp_parameters.put("current_action", "add");
		jsp_parameters.put("next_action", "add_go");
		jsp_parameters.put("next_action_label", "Добавить");
		jsp_parameters.put("show_phones", "false");

		// Установка параметров JSP.
		request.setAttribute("person", empty_person);
		actionDispatcherPerson(request, response);
	}

	private void editPersonGo(HttpServletRequest request, HttpServletResponse response, String id)
			throws ServletException, IOException {
		// Получение записи и её обновление на основе данных из формы.
		Person updatable_person = this.phonebook.getPerson(id);
		updatable_person.setName(request.getParameter("name"));
		updatable_person.setSurname(request.getParameter("surname"));
		updatable_person.setMiddlename(request.getParameter("middlename"));

		// Валидация ФИО.
		String error_message = this.validatePersonFMLName(updatable_person);

		// Если данные верные, можно производить добавление.
		if (error_message.equals("")) {

			// Если запись удалось обновить...
			if (this.phonebook.updatePerson(id, updatable_person)) {
				jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
				jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
			}
			// Если запись НЕ удалось обновить...
			else {
				jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
				jsp_parameters.put("current_action_result_label", "Ошибка обновления");
			}

			actionDispatcherList(request, response);
		}

		// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
		else {

			// Подготовка параметров для JSP.
			jsp_parameters.put("current_action", "edit");
			jsp_parameters.put("next_action", "edit_go");
			jsp_parameters.put("next_action_label", "Сохранить");
			jsp_parameters.put("error_message", error_message);

			// Установка параметров JSP.
			request.setAttribute("person", updatable_person);
			actionDispatcherPerson(request, response);

		}
	}

	private void addPersonGo(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Создание записи на основе данных из формы.
		Person new_person = new Person(request.getParameter("name"), request.getParameter("surname"),
				request.getParameter("middlename"));

		// Валидация ФИО.
		String error_message = this.validatePersonFMLName(new_person);

		// Если данные верные, можно производить добавление.
		if (error_message.equals("")) {

			// Если запись удалось добавить...
			if (this.phonebook.addPerson(new_person)) {
				jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
				jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
			}
			// Если запись НЕ удалось добавить...
			else {
				jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
				jsp_parameters.put("current_action_result_label", "Ошибка добавления");
			}

			actionDispatcherList(request, response);
		}
		// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
		else {
			// Подготовка параметров для JSP.
			jsp_parameters.put("current_action", "add");
			jsp_parameters.put("next_action", "add_go");
			jsp_parameters.put("next_action_label", "Добавить");
			jsp_parameters.put("error_message", error_message);

			// Установка параметров JSP.
			request.setAttribute("person", new_person);
			actionDispatcherPerson(request, response);
		}
	}

	private void setDefaultRequestConfiguration(HttpServletRequest request) throws UnsupportedEncodingException {
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в
		// "кракозябры".
		request.setCharacterEncoding("UTF-8");

		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в
		// JSP.
		request.setAttribute("phonebook", this.phonebook);

		// Диспетчеры для передачи управления на разные JSP (разные представления
		// (view)).
		dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
		dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		dispatcher_for_edit_phone = request.getRequestDispatcher("/EditPhone.jsp");

	}

	private void actionDispatcherList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("jsp_parameters", jsp_parameters);
		dispatcher_for_list.forward(request, response);
	}

	private void actionDispatcherPerson(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("jsp_parameters", jsp_parameters);
		dispatcher_for_manager.forward(request, response);
	}
	

	// Валидация ФИО и генерация сообщения об ошибке в случае невалидных данных.
	private String validatePersonFMLName(Person person) {
		String error_message = "";

		if (!person.validateFMLNamePart(person.getName(), false)) {
			error_message += "Имя должно быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}

		if (!person.validateFMLNamePart(person.getSurname(), false)) {
			error_message += "Фамилия должна быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}

		if (!person.validateFMLNamePart(person.getMiddlename(), true)) {
			error_message += "Отчество должно быть строкой от 0 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}

		return error_message;
	}


}

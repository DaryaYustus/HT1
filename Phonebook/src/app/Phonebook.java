package app;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

import util.DBWorker;

public class Phonebook {

	// Хранилище записей о людях.
	private HashMap<String, Person> persons = new HashMap<String, Person>();

	// Объект для работы с БД.
	private DBWorker db = DBWorker.getInstance();

	// Указатель на экземпляр класса.
	private static Phonebook instance = null;

	// Метод для получения экземпляра класса (реализован Singleton).
	public static Phonebook getInstance() throws ClassNotFoundException, SQLException {
		if (instance == null) {
			instance = new Phonebook();
		}

		return instance;
	}

	// При создании экземпляра класса из БД извлекаются все записи.
	protected Phonebook() throws ClassNotFoundException, SQLException {
		ResultSet db_data = this.db.getDBData("SELECT * FROM `person` ORDER BY `surname` ASC");
		while (db_data.next()) {
			this.persons.put(db_data.getString("id"), new Person(db_data.getString("id"), db_data.getString("name"),
					db_data.getString("surname"), db_data.getString("middlename")));
		}
	}

	// Добавление записи о человеке.
	public boolean addPerson(Person person) {
		ResultSet db_result;
		String query;

		// У человека может не быть отчества.
		if (!person.getSurname().equals("")) {
			query = "INSERT INTO `person` (`name`, `surname`, `middlename`) VALUES ('" + person.getName() + "', '"
					+ person.getSurname() + "', '" + person.getMiddlename() + "')";
		} else {
			query = "INSERT INTO `person` (`name`, `surname`) VALUES ('" + person.getName() + "', '"
					+ person.getSurname() + "')";
		}

		Integer affected_rows = this.db.changeDBData(query);

		// Если добавление прошло успешно...
		if (affected_rows > 0) {
			person.setId(this.db.getLastInsertId().toString());

			// Добавляем запись о человеке в общий список.
			this.persons.put(person.getId(), person);

			return true;
		} else {
			return false;
		}
	}

	// Обновление записи о человеке.
	public boolean updatePerson(String id, Person person) {
		Integer id_filtered = Integer.parseInt(person.getId());
		String query = "";

		// У человека может не быть отчества.
		if (!person.getSurname().equals("")) {
			query = "UPDATE `person` SET `name` = '" + person.getName() + "', `surname` = '" + person.getSurname()
					+ "', `middlename` = '" + person.getMiddlename() + "' WHERE `id` = " + id_filtered;
		} else {
			query = "UPDATE `person` SET `name` = '" + person.getName() + "', `surname` = '" + person.getSurname()
					+ "' WHERE `id` = " + id_filtered;
		}

		Integer affected_rows = this.db.changeDBData(query);

		// Если обновление прошло успешно...
		if (affected_rows > 0) {
			// Обновляем запись о человеке в общем списке.
			this.persons.put(person.getId(), person);
			return true;
		} else {
			return false;
		}
	}

	// Удаление записи о человеке.
	public boolean deletePerson(String id) {
		if ((id != null) && (!id.equals("null"))) {
			int filtered_id = Integer.parseInt(id);

			Integer affected_rows = this.db.changeDBData("DELETE FROM `person` WHERE `id`=" + filtered_id);

			// Если удаление прошло успешно...
			if (affected_rows > 0) {
				// Удаляем запись о человеке из общего списка.
				this.persons.remove(id);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	// !Добавление номере телефона
	public boolean addPhoneNumber(String id, String phone) throws SQLException {
		String query = "insert into `phone` values (NULL, '" + id + "', '" + phone + "')";
		Integer affected_rows = this.db.changeDBData(query);

		// Если обновление прошло успешно...
		if (affected_rows > 0) {
			// Обновляем запись о телефоне для человека.
			this.persons.get(id).getPhones().put(this.db.getLastInsertId().toString(), phone);
			return true;
		} else {
			return false;
		}
	}

	// Обновление записи о номере телефона.
	public boolean updatePhoneNumber(String id, String old_phone, String new_phone) throws SQLException {
		String query = "";
		int phone_id = 0;

		ResultSet db_phone = this.db
				.getDBData("SELECT id FROM phone WHERE number LIKE '%" + old_phone.trim() + "%' and owner = " + id);
		while (db_phone.next()) {
			phone_id = db_phone.getInt("id");
		}

		query = "UPDATE `phone` SET number = '" + new_phone + "', owner='" + id + "' WHERE id = " + phone_id;
		Integer affected_rows = this.db.changeDBData(query);

		// Если обновление прошло успешно...
		if (affected_rows > 0) {
			// Обновляем запись о человеке в общем списке.
			this.persons.get(id).getPhones().put(String.valueOf(phone_id), new_phone);
			return true;
		} else {
			return false;
		}
	}

	// Удаление записи о номере телефона.
	public boolean deletePhoneNumber(String id, String phone) {

		if (id != null) {
			int phoneId = 0;
			ResultSet db_phone = this.db.getDBData("SELECT id FROM phone WHERE number LIKE '%" + phone.trim() + "%'");

			try {
				while (db_phone.next()) {
					phoneId = db_phone.getInt("id");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Integer affected_rows = this.db
					.changeDBData("DELETE FROM phone WHERE number LIKE '%" + phone.trim() + "%' and owner = " + id);

			// Если удаление прошло успешно...
			if (affected_rows > 0) {
				this.persons.get(id).getPhones().remove(String.valueOf(phoneId));
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	// +++++++++++++++++++++++++++++++++++++++++
	// Геттеры и сеттеры
	public HashMap<String, Person> getContents() {
		return persons;
	}

	public Person getPerson(String id) {
		return this.persons.get(id);
	}
	// Геттеры и сеттеры
	// -----------------------------------------

}

package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBWorker {

	// Количество рядов таблицы, затронутых последним запросом.
	private Integer affected_rows = 0;

	// Значение автоинкрементируемого первичного ключа, полученное после
	// добавления новой записи.
	private Integer last_insert_id = 0;

	// Указатель на экземпляр класса.
	private static DBWorker instance = null;

	private Statement statement;
	private Connection connect;

	// Метод для получения экземпляра класса (реализован Singleton).
	public static DBWorker getInstance() {
		if (instance == null) {
			instance = new DBWorker();
		}

		return instance;
	}

	// "Заглушка", чтобы экземпляр класса нельзя было получить напрямую.
	private DBWorker() {

	}

	// Выполнение запросов на выборку данных.
	public ResultSet getDBData(String query) {
		initializeDBConnection();
		ResultSet resultSet = null;
		try {
			resultSet = statement.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultSet;

	}

	// Выполнение запросов на модификацию данных.
	public Integer changeDBData(String query) {
		initializeDBConnection();
		try {
			this.affected_rows = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			// Получаем last_insert_id() для операции вставки.
			ResultSet rs = statement.getGeneratedKeys();
			if (rs.next()) {
				this.last_insert_id = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return this.affected_rows;
	}

	//установка соединения с базой данных
	private void initializeDBConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connect = DriverManager.getConnection(
					"jdbc:mysql://localhost/phonebook?user=root&password=root&useUnicode=true&characterEncoding=UTF-8&characterSetResults=utf8&connectionCollation=utf8_general_ci");
			statement = connect.createStatement();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++
	// Геттеры и сеттеры.
	public Integer getAffectedRowsCount() {
		return this.affected_rows;
	}

	public Integer getLastInsertId() {
		return this.last_insert_id;
	}
	// Геттеры и сеттеры.
	// -------------------------------------------------
}

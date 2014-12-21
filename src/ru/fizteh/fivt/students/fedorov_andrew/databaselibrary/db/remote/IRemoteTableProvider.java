package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.List;

interface IRemoteTableProvider extends Remote {
    /**
     * Возвращает таблицу с указанным названием.
     *
     * Последовательные вызовы метода с одинаковыми аргументами должны возвращать один и тот же объект
     * таблицы,
     * если он не был удален с помощью {@link #removeTable(String)}.
     * @param name
     *         Название таблицы.
     * @return Объект, представляющий таблицу. Если таблицы с указанным именем не существует, возвращает null.
     * @throws IllegalArgumentException
     *         Если название таблицы null или имеет недопустимое значение.
     */
    RemoteTableStub getTable(String name) throws RemoteException;

    /**
     * Создаёт таблицу с указанным названием.
     * Создает новую таблицу. Совершает необходимые дисковые операции.
     * @param name
     *         Название таблицы.
     * @param columnTypes
     *         Типы колонок таблицы. Не может быть пустой.
     * @return Объект, представляющий таблицу. Если таблица с указанным именем существует, возвращает null.
     * @throws IllegalArgumentException
     *         Если название таблицы null или имеет недопустимое значение. Если список типов
     *         колонок null или содержит недопустимые значения.
     * @throws java.io.IOException
     *         При ошибках ввода/вывода.
     */
    RemoteTableStub createTable(String name, List<Class<?>> columnTypes) throws IOException;

    /**
     * Удаляет существующую таблицу с указанным названием.
     *
     * Объект удаленной таблицы, если был кем-то взят с помощью {@link #getTable(String)},
     * с этого момента должен бросать {@link IllegalStateException}.
     * @param name
     *         Название таблицы.
     * @throws IllegalArgumentException
     *         Если название таблицы null или имеет недопустимое значение.
     * @throws IllegalStateException
     *         Если таблицы с указанным названием не существует.
     * @throws java.io.IOException
     *         - при ошибках ввода/вывода.
     */
    void removeTable(String name) throws IOException;

    /**
     * Преобразовывает строку в объект {@link ru.fizteh.fivt.storage.structured.Storeable}, соответствующий
     * структуре таблицы.
     * @param table
     *         Таблица, которой должен принадлежать {@link ru.fizteh.fivt.storage.structured.Storeable}.
     * @param value
     *         Строка, из которой нужно прочитать {@link ru.fizteh.fivt.storage.structured.Storeable}.
     * @return Прочитанный {@link ru.fizteh.fivt.storage.structured.Storeable}.
     * @throws java.text.ParseException
     *         - при каких-либо несоответстиях в прочитанных данных.
     */
    Storeable deserialize(Table table, String value) throws RemoteException, ParseException;

    /**
     * Преобразовывает объект {@link ru.fizteh.fivt.storage.structured.Storeable} в строку.
     * @param table
     *         Таблица, которой должен принадлежать {@link ru.fizteh.fivt.storage.structured.Storeable}.
     * @param value
     *         {@link ru.fizteh.fivt.storage.structured.Storeable}, который нужно записать.
     * @return Строка с записанным значением.
     * @throws ru.fizteh.fivt.storage.structured.ColumnFormatException
     *         При несоответствии типа в {@link ru.fizteh.fivt.storage.structured.Storeable} и типа колонки в
     *         таблице.
     */
    String serialize(Table table, Storeable value) throws RemoteException, ColumnFormatException;

    /**
     * Создает новый пустой {@link ru.fizteh.fivt.storage.structured.Storeable} для указанной таблицы.
     * @param table
     *         Таблица, которой должен принадлежать {@link ru.fizteh.fivt.storage.structured.Storeable}.
     * @return Пустой {@link ru.fizteh.fivt.storage.structured.Storeable}, нацеленный на использование с этой
     * таблицей.
     */
    Storeable createFor(Table table) throws RemoteException;

    /**
     * Создает новый {@link ru.fizteh.fivt.storage.structured.Storeable} для указанной таблицы, подставляя
     * туда переданные значения.
     * @param table
     *         Таблица, которой должен принадлежать {@link ru.fizteh.fivt.storage.structured.Storeable}.
     * @param values
     *         Список значений, которыми нужно проинициализировать поля Storeable.
     * @return {@link ru.fizteh.fivt.storage.structured.Storeable}, проинициализированный переданными
     * значениями.
     * @throws ColumnFormatException
     *         При несоответствии типа переданного значения и колонки.
     * @throws IndexOutOfBoundsException
     *         При несоответствии числа переданных значений и числа колонок.
     */
    Storeable createFor(Table table, List<?> values)
            throws RemoteException, ColumnFormatException, IndexOutOfBoundsException;

    /**
     * Возвращает имена существующих таблиц, которые могут быть получены с помощью {@link #getTable(String)}.
     * @return Имена существующих таблиц.
     */
    List<String> getTableNames() throws RemoteException;
}

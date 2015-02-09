package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

interface IRemoteTable extends Remote {
    /**
     * Возвращает название таблицы или индекса.
     * @return Название таблицы.
     */
    String getName() throws RemoteException;

    /**
     * Получает значение по указанному ключу.
     * @param key
     *         Ключ для поиска значения. Не может быть null.
     *         Для индексов по не-строковым полям аргумент представляет собой сериализованное значение
     *         колонки.
     *         Его потребуется распарсить.
     * @return Значение. Если не найдено, возвращает null.
     * @throws IllegalArgumentException
     *         Если значение параметра key является null.
     */
    Storeable get(String key) throws RemoteException;

    /**
     * Устанавливает значение по указанному ключу.
     * @param key
     *         Ключ для нового значения. Не может быть null.
     * @param value
     *         Новое значение. Не может быть null.
     * @return Значение, которое было записано по этому ключу ранее. Если ранее значения не было записано,
     * возвращает null.
     * @throws IllegalArgumentException
     *         Если значение параметров key или value является null.
     * @throws ru.fizteh.fivt.storage.structured.ColumnFormatException
     *         - при попытке передать Storeable с колонками другого типа.
     */
    Storeable put(String key, Storeable value) throws ColumnFormatException, RemoteException;

    /**
     * Удаляет значение по указанному ключу.
     * @param key
     *         Ключ для поиска значения. Не может быть null.
     * @return Предыдущее значение. Если не найдено, возвращает null.
     * @throws IllegalArgumentException
     *         Если значение параметра key является null.
     */
    Storeable remove(String key) throws RemoteException;

    /**
     * Возвращает количество ключей в таблице. Возвращает размер текущей версии, с учётом незафиксированных
     * изменений.
     * @return Количество ключей в таблице.
     */
    int size() throws RemoteException;

    /**
     * Выводит список ключей таблицы, с учётом незафиксированных изменений.
     * @return Список ключей.
     */
    List<String> list() throws RemoteException;

    /**
     * Выполняет фиксацию изменений.
     * @return Число записанных изменений.
     * @throws java.io.IOException
     *         если произошла ошибка ввода/вывода. Целостность таблицы не гарантируется.
     */
    int commit() throws IOException;

    /**
     * Выполняет откат изменений с момента последней фиксации.
     * @return Число откаченных изменений.
     */
    int rollback() throws RemoteException;

    /**
     * Возвращает количество изменений, ожидающих фиксации.
     * @return Количество изменений, ожидающих фиксации.
     */
    int getNumberOfUncommittedChanges() throws RemoteException;

    /**
     * Возвращает количество колонок в таблице.
     * @return Количество колонок в таблице.
     */
    int getColumnsCount() throws RemoteException;

    /**
     * Возвращает тип значений в колонке.
     * @param columnIndex
     *         Индекс колонки. Начинается с нуля.
     * @return Класс, представляющий тип значения.
     * @throws IndexOutOfBoundsException
     *         - неверный индекс колонки
     */
    Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException, RemoteException;
}

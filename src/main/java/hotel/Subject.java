package hotel;
public interface Subject<K, E> {
    void subscribe(K key, Observer<E> observer);
    void unsubscribe(K key, Observer<E> observer);
}

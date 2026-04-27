package structures;

public class HashNode<K, V> {
    private K key;
    private V value;
    private HashNode<K, V> next; // Para manejar colisiones cerradas (Chaining)

    public HashNode(K key, V value) {
        this.key = key;
        this.value = value;
        this.next = null;
    }

    public K getKey() { return key; }
    public V getValue() { return value; }
    public void setValue(V value) { this.value = value; }
    
    public HashNode<K, V> getNext() { return next; }
    public void setNext(HashNode<K, V> next) { this.next = next; }
}

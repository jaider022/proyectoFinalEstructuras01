package structures;

/**
 * Tabla Hash genérica optimizada.
 * Permite buscar clientes por ID o Inmuebles por Código en tiempo cercano a O(1).
 * Implementa resolución de colisiones mediante encadenamiento (Chaining).
 */
public class CustomHashTable<K, V> {
    // Arreglo de buckets donde almacenaremos las cabezas de las listas enlazadas
    private HashNode<K, V>[] buckets;
    private int capacity;
    private int size;

    @SuppressWarnings("unchecked")
    public CustomHashTable(int capacity) {
        this.capacity = capacity;
        this.buckets = new HashNode[capacity];
        this.size = 0;
    }

    // Calcula de forma matemática el índice (la "caja" o bucket) para una llave
    private int getBucketIndex(K key) {
        int hashCode = key.hashCode();
        return Math.abs(hashCode % capacity);
    }

    // Inserta o actualiza un par Llave/Valor
    public void put(K key, V value) {
        int index = getBucketIndex(key);
        HashNode<K, V> head = buckets[index];

        // 1. Revisar si la clave ya existe para actualizar su valor
        HashNode<K, V> current = head;
        while (current != null) {
            if (current.getKey().equals(key)) {
                current.setValue(value);
                return;
            }
            current = current.getNext();
        }

        // 2. Si no existe, lo insertamos al principio del bucket (encadenamiento/chaining)
        size++;
        HashNode<K, V> newNode = new HashNode<>(key, value);
        newNode.setNext(head);
        buckets[index] = newNode;
    }

    // Buscar el valor por su llave exacta
    public V get(K key) {
        int index = getBucketIndex(key);
        HashNode<K, V> head = buckets[index];

        HashNode<K, V> current = head;
        while (current != null) {
            if (current.getKey().equals(key)) {
                return current.getValue(); // Lo encontramos al instante O(1) average
            }
            current = current.getNext();
        }
        return null; // no encontrado
    }

    // Eliminar por llave
    public void remove(K key) {
        int index = getBucketIndex(key);
        HashNode<K, V> head = buckets[index];

        HashNode<K, V> current = head;
        HashNode<K, V> prev = null;
        
        while (current != null) {
            if (current.getKey().equals(key)) {
                break;
            }
            prev = current;
            current = current.getNext();
        }

        if (current == null) return; // no se encontro la llave en esta lista

        size--;
        if (prev != null) {
            prev.setNext(current.getNext());
        } else {
            // Era el primer nodo de la lista de ese bucket
            buckets[index] = current.getNext();
        }
    }

    public int getSize() { return size; }
    public boolean isEmpty() { return size == 0; }

    // Nuevo: Retornar todos los valores en una lista
    public CustomList<V> toList() {
        CustomList<V> list = new CustomList<>();
        for (int i = 0; i < capacity; i++) {
            HashNode<K, V> current = buckets[i];
            while (current != null) {
                list.add(current.getValue());
                current = current.getNext();
            }
        }
        return list;
    }
}

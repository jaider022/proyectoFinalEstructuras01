package structures;

/**
 * Cola Genérica (Queue - FIFO: First In, First Out)
 * Especializada para atender clientes o visitas en estricto orden de llegada.
 */
public class CustomQueue<T> {
    private Node<T> front;
    private Node<T> rear;
    private int size;

    public CustomQueue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    // Agregar al final de la cola (Agendar a alguien)
    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            rear.setNext(newNode);
            rear = newNode;
        }
        size++;
    }

    // Atender (extraer) del frente de la cola (al que lleva más tiempo esperando)
    public T dequeue() {
        if (isEmpty()) return null;
        T data = front.getData();
        front = front.getNext();
        if (front == null) {
            rear = null; // Si la cola quedó vacía, ambos apuntadores deben ser null
        }
        size--;
        return data;
    }

    // Ver quién está de primero en la cola sin sacarlo de ella
    public T peek() {
        if (isEmpty()) return null;
        return front.getData();
    }

    public boolean isEmpty() { return size == 0; }
    public int getSize() { return size; }

    public boolean anyMatch(java.util.function.Predicate<T> predicate) {
        Node<T> current = front;
        while (current != null) {
            if (predicate.test(current.getData())) {
                return true;
            }
            current = current.getNext();
        }
        return false;
    }
}

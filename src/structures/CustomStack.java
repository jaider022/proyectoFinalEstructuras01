package structures;

/**
 * Pila Genérica (Stack - LIFO: Last In, First Out)
 * Permite insertar operaciones y deshacerlas si es necesario.
 */
public class CustomStack<T> {
    private Node<T> top;
    private int size;

    public CustomStack() {
        this.top = null;
        this.size = 0;
    }

    // Insertar en la cima de la pila
    public void push(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.setNext(top);
        top = newNode;
        size++;
    }

    // Extraer (deshacer) de la cima
    public T pop() {
        if (isEmpty()) return null;
        T data = top.getData();
        top = top.getNext();
        size--;
        return data;
    }

    // Ver la cima sin extraer
    public T peek() {
        if (isEmpty()) return null;
        return top.getData();
    }

    public boolean isEmpty() { return size == 0; }
    public int getSize() { return size; }
}

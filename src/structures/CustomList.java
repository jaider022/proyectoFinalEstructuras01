package structures;

/**
 * Lista Enlazada Simple, construida con tipos genéricos <T>
 * Puede almacenar Inmuebles, Clientes, Operaciones o cualquier objeto.
 */
public class CustomList<T> {
    private Node<T> head;
    private int size;

    public CustomList() {
        this.head = null;
        this.size = 0;
    }

    // Añade un elemento al final de la lista
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.getNext() != null) {
                current = current.getNext();
            }
            current.setNext(newNode);
        }
        size++;
    }

    // Elimina un elemento por coincidencia de objeto
    public void remove(T data) {
        if (head == null) return;
        
        if (head.getData().equals(data)) {
            head = head.getNext();
            size--;
            return;
        }
        
        Node<T> current = head;
        while (current.getNext() != null && !current.getNext().getData().equals(data)) {
            current = current.getNext();
        }
        
        if (current.getNext() != null) {
            current.setNext(current.getNext().getNext());
            size--;
        }
    }

    // Encuentra el índice de un elemento
    public int indexOf(T data) {
        Node<T> current = head;
        int index = 0;
        while (current != null) {
            if (current.getData().equals(data)) {
                return index;
            }
            current = current.getNext();
            index++;
        }
        return -1;
    }

    // Obtener un elemento por su índice (posición)
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de los límites.");
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        return current.getData();
    }

    public void printList() {
        Node<T> current = head;
        while (current != null) {
            System.out.println(" - " + current.getData().toString());
            current = current.getNext();
        }
    }
    
    public int getSize() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void set(int index, T data) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de los límites.");
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        current.setData(data);
    }
}

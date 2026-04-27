package structures;

/**
 * Cola de Prioridad Genérica implementada con un Max-Heap (Montículo).
 * Los elementos con mayor valor de prioridad salen primero.
 */
public class CustomPriorityQueue<T> {
    private PriorityElement<T>[] heap;
    private int size;
    private int capacity;

    @SuppressWarnings("unchecked")
    public CustomPriorityQueue(int capacity) {
        this.capacity = capacity;
        this.heap = new PriorityElement[capacity];
        this.size = 0;
    }

    // Clase interna para envolver el dato con su prioridad
    private static class PriorityElement<T> {
        T data;
        int priority;

        PriorityElement(T data, int priority) {
            this.data = data;
            this.priority = priority;
        }
    }

    public void enqueue(T data, int priority) {
        if (size == capacity) {
            throw new RuntimeException("Priority Queue llena");
        }

        PriorityElement<T> newElement = new PriorityElement<>(data, priority);
        heap[size] = newElement;
        bubbleUp(size);
        size++;
    }

    public T dequeue() {
        if (isEmpty()) return null;

        T rootData = heap[0].data;
        heap[0] = heap[size - 1];
        size--;
        bubbleDown(0);
        return rootData;
    }

    private void bubbleUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (heap[index].priority > heap[parentIndex].priority) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    private void bubbleDown(int index) {
        while (true) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int largest = index;

            if (leftChild < size && heap[leftChild].priority > heap[largest].priority) {
                largest = leftChild;
            }

            if (rightChild < size && heap[rightChild].priority > heap[largest].priority) {
                largest = rightChild;
            }

            if (largest != index) {
                swap(index, largest);
                index = largest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        PriorityElement<T> temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int getSize() {
        return size;
    }

    public T peek() {
        if (isEmpty()) return null;
        return heap[0].data;
    }

    public boolean anyMatch(java.util.function.Predicate<T> predicate) {
        for (int i = 0; i < size; i++) {
            if (predicate.test(heap[i].data)) {
                return true;
            }
        }
        return false;
    }
}

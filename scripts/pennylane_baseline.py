import pennylane as qml
import time
import numpy as np

def run_baseline(num_qubits):
    print(f"--- Pornire etalon PennyLane pentru {num_qubits} qubiti (Dense Simulator) ---")
    
    # Initializare dispozitiv standard dens
    dev = qml.device("default.qubit", wires=num_qubits)

    @qml.qnode(dev)
    def circuit():
        # Aplica o serie de operatii pentru a forta calculul pe matrice densa globala
        for i in range(num_qubits):
            qml.Hadamard(wires=i)
        for i in range(num_qubits - 1):
            qml.CNOT(wires=[i, i+1])
        return qml.state()

    start_time = time.time()
    state = circuit()
    end_time = time.time()
    
    execution_time_ms = (end_time - start_time) * 1000
    print(f"Rezultat PennyLane: {execution_time_ms:.2f} ms pentru {2**num_qubits} stari.")
    return execution_time_ms

if __name__ == "__main__":
    # Testam securizat doar pana la 20 de qubiti (~1 milion de stari dense)
    # Daca am seta 24+ aici, simulatorul ar intra in faza de swapping si ar bloca pipeline-ul
    run_baseline(num_qubits=20)

package org.example.services;

import org.example.models.Compra;
import org.example.repositories.EmpaquetadorRepository;
import java.sql.SQLException;
import java.util.List;

public class EmpaquetadorService {

    private final EmpaquetadorRepository repository = new EmpaquetadorRepository();

    public List<Compra> getWorkQueue(Long empaquetadorId) throws SQLException {
        return repository.findWorkQueueByEmpaquetadorId(empaquetadorId);
    }
}
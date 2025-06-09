package com.iftm.client.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.services.exceptions.ResourceNotFoundException;

import java.time.Instant;

@SpringBootTest
@Transactional
public class ClientServiceIntegrationTest {

    @Autowired
    private ClientService clientService;

    private Long existingId;
    private Long nonExistingId;
    private int countTotalClients;
    private Double incomeToSearch;
    private int countClientsByIncome;
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() throws Exception { // configuração inicial antes de cada teste
        // trhows exeption é opcional, mas é uma boa prática para indicar que o método pode lançar exceções
        existingId = 1L;
        nonExistingId = 1000L;
        countTotalClients = 12;
        incomeToSearch = 4500.0;
        countClientsByIncome = 2;
        pageRequest = PageRequest.of(0, 10);
    }

    @Test
    public void deleteDeveApagarRecursoQuandoIdExiste() {
        // Act
        Assertions.assertDoesNotThrow(() -> {
            clientService.delete(existingId);
        });

        // Assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            clientService.findById(existingId);
        });
    }

    @Test
    public void deleteDeveLancarResourceNotFoundExceptionQuandoIdNaoExiste() {
        // Act e Assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            clientService.delete(nonExistingId);
        });
    }

    @Test
    public void findAllPagedDeveRetornarPaginaComTodosClientes() {

        // Act
        Page<ClientDTO> result = clientService.findAllPaged(pageRequest);

        // Assert
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(countTotalClients, result.getTotalElements());
    }
    
    @Test
    public void findByIncomeDeveRetornarPaginaComClientesDeRendaCorrespondente() {
        // Act
        Page<ClientDTO> result = clientService.findByIncome(incomeToSearch, pageRequest);

        // Assert
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(countClientsByIncome, result.getTotalElements());
        Assertions.assertEquals(incomeToSearch, result.getContent().get(0).getIncome());
    }

    @Test
    public void findByIdDeveRetornarClientDTOQuandoIdExiste() {
        // Act
        ClientDTO resultDTO = clientService.findById(existingId);

        // Assert
        Assertions.assertNotNull(resultDTO);
        Assertions.assertEquals(existingId, resultDTO.getId());
    }

    @Test
    public void findByIdDeveLancarResourceNotFoundExceptionQuandoIdNaoExiste() {
        // Act e assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            clientService.findById(nonExistingId);
        });
    }

    @Test
    public void updateDeveRetornarClientDTOQuandoIdExiste() {
        //Assign
        ClientDTO dto = new ClientDTO(null, "Nome Atualizado", "11122233344", 7500.0, Instant.now(), 1);

        // Act
        ClientDTO updatedDto = clientService.update(existingId, dto);

        // Assert
        Assertions.assertNotNull(updatedDto);
        Assertions.assertEquals(existingId, updatedDto.getId());
        Assertions.assertEquals("Nome Atualizado", updatedDto.getName());
        Assertions.assertEquals(7500.0, updatedDto.getIncome());
    }

    @Test
    public void updateDeveLancarResourceNotFoundExceptionQuandoIdNaoExiste() {
        // Assign
        ClientDTO dto = new ClientDTO(); 

        // Act e Assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            clientService.update(nonExistingId, dto);
        });
    }
    
    @Test
    public void insertDeveCriarERetornarClientDTO() {
        // Assign
        ClientDTO newDto = new ClientDTO(null, "Novo Cliente", "12312312399", 10000.0, Instant.now(), 0);

        // Act
        ClientDTO resultDto = clientService.insert(newDto);

        // Assert
        Assertions.assertNotNull(resultDto.getId());
        Assertions.assertEquals("Novo Cliente", resultDto.getName());
        Assertions.assertEquals(10000.0, resultDto.getIncome());
        Assertions.assertEquals(countTotalClients + 1, clientService.findAllPaged(PageRequest.of(0, 20)).getTotalElements());
    }
}
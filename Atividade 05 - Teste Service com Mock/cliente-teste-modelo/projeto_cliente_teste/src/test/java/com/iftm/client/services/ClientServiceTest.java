package com.iftm.client.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.repositories.ClientRepository;
import com.iftm.client.services.exceptions.ResourceNotFoundException;

@ExtendWith(SpringExtension.class)
// A anotação @ExtendWith(SpringExtension.class) é usada para integrar o JUnit 5
// com o Spring TestContext Framework,
public class ClientServiceTest {

    @InjectMocks
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;

    Long existingId;
    long nonExistingId;
    private Client client; // Adicionar para ter um cliente de exemplo
    private ClientDTO clientDTO; // Adicionar para o DTO do cliente
    private PageRequest pageRequest; // Adicionar para o PageRequest
    private Page<Client> clientPage; // Adicionar para a página de clientes
    private Double existingIncome;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        existingIncome = 5000.0;

        pageRequest = PageRequest.of(0, 10); // Página 0, 10 elementos por página

        // criando um cliente de exemplo
        // Cliente de exemplo com a renda 'existingIncome'
        client = new Client(existingId, "Maria Brown", "12345678901", existingIncome,
                Instant.parse("1990-07-20T10:00:00Z"), 1);

        // clientDTO usado como entrada para o teste de update,
        clientDTO = new ClientDTO(existingId, "Maria Silva Update", "11122233344", 6000.0,
                Instant.parse("1992-08-15T10:00:00Z"), 2);

        // uma Page<Client> de exemplo para ser retornada pelo mock do repositório
        List<Client> clientList = List.of(client);
        clientPage = new PageImpl<>(clientList, pageRequest, clientList.size());

        // --- configurando mocks ---

        // para deleteById
        Mockito.doNothing().when(clientRepository).deleteById(existingId);
        Mockito.doThrow(EmptyResultDataAccessException.class).when(clientRepository).deleteById(nonExistingId);

        // para findAll(PageRequest)
        // Quando o repository.findAll for chamado com QUALQUER PageRequest, ele
        // retornará nosso clientPage.
        Mockito.when(clientRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(clientPage);

        // Mock para findByIncome(Double income, Pageable pageable)
        // quando o repository.findByIncome for chamado com existingIncome e qualquer
        // Pageable,
        // ele retornará nosso clientPage (que contém o 'client' com 'existingIncome').
        Mockito.when(clientRepository.findByIncome(Mockito.eq(existingIncome), Mockito.any(Pageable.class)))
                .thenReturn(clientPage);

        // Mocks para findById
        // Quando findById for chamado com existingId, retorna um Optional contendo
        // nosso 'client'
        Mockito.when(clientRepository.findById(existingId)).thenReturn(Optional.of(client));
        // Quando findById for chamado com nonExistingId, retorna um Optional vazio
        Mockito.when(clientRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Mocks para os métodos update e insert
        Mockito.when(clientRepository.getOne(existingId)).thenReturn(client);
        Mockito.when(clientRepository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);
        Mockito.when(clientRepository.save(ArgumentMatchers.any(Client.class))).thenReturn(client);

    }

    @Test
    public void deleteDeveRetornarVazioQuandoIdExiste() {
        Assertions.assertDoesNotThrow(() -> {
            clientService.delete(existingId);
        });

        Mockito.verify(clientRepository, Mockito.times(1)).deleteById(existingId);
    }

    @Test
    public void deleteDeveLancarEmptyResultDataAccessExceptionQuandoIdNaoExiste() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            clientService.delete(nonExistingId);
        });

        Mockito.verify(clientRepository, Mockito.times(1)).deleteById(nonExistingId);

    }

    @Test
    public void findAllPagedDeveRetornarPaginaComTodosClientes() {
        // Ação: Chama o método do serviço que queremos testar
        Page<ClientDTO> paginaRetornada = clientService.findAllPaged(pageRequest);

        Assertions.assertNotNull(paginaRetornada);

        // verifica se o método findAll do repositório foi chamado uma vez com o
        // pageRequest correto
        // Se o mock no setUp foi específico (com a instância pageRequest), a
        // verificação aqui também deve ser.
        Mockito.verify(clientRepository, Mockito.times(1)).findAll(pageRequest);

        // verifica o conteúdo da página (opcional, mas bom para garantir o mapeamento)
        // verifica se o número de elementos na página DTO é o mesmo da página de
        // entidade original
        Assertions.assertEquals(clientPage.getTotalElements(), paginaRetornada.getTotalElements());
    }

    @Test
    public void findByIncomeDeveRetornarPaginaComClientesComRenda() {

        Page<ClientDTO> paginaRetornada = clientService.findByIncome(existingIncome, pageRequest);

        // verifica se a página retornada não é nula
        Assertions.assertNotNull(paginaRetornada);

        // verifica se o método findByIncome do repositório foi chamado uma vez com os
        // parâmetros corretos
        Mockito.verify(clientRepository, Mockito.times(1)).findByIncome(existingIncome, pageRequest);

        // verifica se a página não está vazia
        Assertions.assertFalse(paginaRetornada.isEmpty());

        Assertions.assertEquals(1, paginaRetornada.getTotalElements());
        // verifica o cliente na página retornada tem o income pesquisado
        Assertions.assertEquals(existingIncome, paginaRetornada.getContent().get(0).getIncome());
    }

    // --- NOVOS TESTES ABAIXO ---

    @Test
    public void findByIdDeveRetornarCliendeCasoIdExista() {
        ClientDTO resultDTO = clientService.findById(existingId);

        // verifica se o DTO retornado não é nulo
        Assertions.assertNotNull(resultDTO);

        // verifica se os dados no DTO correspondem aos do cliente original
        Assertions.assertEquals(existingId, resultDTO.getId());
        Assertions.assertEquals(client.getName(), resultDTO.getName());
        Assertions.assertEquals(client.getCpf(), resultDTO.getCpf());
        Assertions.assertEquals(client.getIncome(), resultDTO.getIncome());

        // verifica se o método findById do repositório foi chamado uma vez com o ID
        // correto
        Mockito.verify(clientRepository, Mockito.times(1)).findById(existingId);
    }

    @Test
    public void findByIdLancaResourceNotFoundExceptioQuandoIdInexiste() {
        // verifica se a exceção ResourceNotFoundException é lançada
        // ao tentar buscar um cliente com ID inexistente.
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            clientService.findById(nonExistingId);
        });

        // verifica se o método findById foi chamado uma vez com o ID (não existente)
        Mockito.verify(clientRepository, Mockito.times(1)).findById(nonExistingId);
    }


    @Test
    public void updateDeveriaRetornarClientDTQQuandoIdExiste() {
        ClientDTO resultDTO = clientService.update(existingId, clientDTO);

        // garante que o DTO retornado não seja nulo
        Assertions.assertNotNull(resultDTO);

        // garante que os dados retornados no DTO são os dados atualizados
        Assertions.assertEquals(existingId, resultDTO.getId());
        Assertions.assertEquals(clientDTO.getName(), resultDTO.getName());
        Assertions.assertEquals(clientDTO.getCpf(), resultDTO.getCpf());
        Assertions.assertEquals(clientDTO.getIncome(), resultDTO.getIncome());

        //  garante que o método 'getOne' e 'save' do repositório foram
        // chamados uma vez
        Mockito.verify(clientRepository, Mockito.times(1)).getOne(existingId);
        Mockito.verify(clientRepository, Mockito.times(1)).save(client);
    }

    @Test
    public void updateDeveriaLancarResourceNotFoundExceptionQuandoIdNaoExiste() {
        // confirma que a exceção esperada é lançada ao tentar atualizar um
        // ID inexistente
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            clientService.update(nonExistingId, clientDTO);
        });

        // verifica se o método 'getOne' do repositório foi chamado
        Mockito.verify(clientRepository, Mockito.times(1)).getOne(nonExistingId);
    }

    @Test
    public void insertDeveriaRetornarClientDTO() {

        ClientDTO resultDTO = clientService.insert(clientDTO);

        // verifica se o DTO retornado não é nulo e contém os dados
        // esperados
        Assertions.assertNotNull(resultDTO);
        Assertions.assertEquals(client.getName(), resultDTO.getName());
        Assertions.assertEquals(client.getCpf(), resultDTO.getCpf());
        Assertions.assertEquals(client.getIncome(), resultDTO.getIncome());

        // verifica o método 'save' do repositório foi chamado uma vez
        Mockito.verify(clientRepository, Mockito.times(1)).save(ArgumentMatchers.any(Client.class));
    }

}

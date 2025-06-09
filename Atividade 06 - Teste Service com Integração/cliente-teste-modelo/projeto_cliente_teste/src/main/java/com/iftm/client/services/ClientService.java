package com.iftm.client.services;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.repositories.ClientRepository;
import com.iftm.client.services.exceptions.DatabaseException;
import com.iftm.client.services.exceptions.ResourceNotFoundException;

@Service
public class ClientService {

	@Autowired // injeção de dependência do Spring
	private ClientRepository repository;

	@Transactional(readOnly = true) // transactional para não bloquear o banco de dados
	// esse método realiza uma consulta no banco de dados e retorna uma lista de
	// clientes DTOs paginada
	public Page<ClientDTO> findAllPaged(PageRequest pageRequest) {
		Page<Client> list = repository.findAll(pageRequest);
		return list.map(x -> new ClientDTO(x));
	}

	// Page é uma interface que representa uma página de resultados,
	// PageRequest é uma classe que implementa a interface Pageable e é usada para
	// definir os parâmetros de paginação.

	@Transactional(readOnly = true)
	public ClientDTO findById(Long id) {
		Optional<Client> obj = repository.findById(id); // oPTional é uma classe que pode ou não conter um valor,
														// evitando NullPointerException
		Client entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		// entity é o objeto Client retornado pelo Optional, se ele existir
		// essa linha lança uma exceção ResourceNotFoundException se o cliente não for
		// encontrado
		// orElseThrow é um método que retorna o valor contido no Optional ou lança uma
		// exceção se o valor não estiver presente
		return new ClientDTO(entity);
	}

	@Transactional
	public ClientDTO insert(ClientDTO dto) {
		Client entity = dto.toEntity();
		entity = repository.save(entity);
		return new ClientDTO(entity);
	}

	@Transactional
	public ClientDTO update(Long id, ClientDTO dto) {
		// o try-cath tenta buscar o cliente pelo id
		// se não encontrar, lança uma exceção ResourceNotFoundException
		// se encontrar, atualiza os dados do cliente e salva no banco de dados
		// o método updateData é responsável por atualizar os dados do cliente com os
		// dados do DTO
		try {
			Client entity = repository.getOne(id);
			updateData(entity, dto);
			entity = repository.save(entity);
			return new ClientDTO(entity);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
	}

	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}

	private void updateData(Client entity, ClientDTO dto) {
		entity.setName(dto.getName());
		entity.setCpf(dto.getCpf());
		entity.setIncome(dto.getIncome());
		entity.setBirthDate(dto.getBirthDate());
		entity.setChildren(dto.getChildren());
	}

	@Transactional(readOnly = true) //método que busca clientes por renda
	// o método recebe um valor de renda e um PageRequest para paginação
	// ele retorna uma página de clientes DTOs que possuem a renda especificada
	public Page<ClientDTO> findByIncome(Double income, PageRequest pageRequest) {
		Page<Client> page = repository.findByIncome(income, pageRequest);
		return page.map(x -> new ClientDTO(x));
	}

}

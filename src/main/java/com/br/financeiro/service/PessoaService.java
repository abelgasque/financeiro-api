package com.br.financeiro.service;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.br.financeiro.exceptionhandler.CustomRuntimeException;
import com.br.financeiro.model.Pessoa;
import com.br.financeiro.model.Usuario;
import com.br.financeiro.model.filter.PessoaFilter;
import com.br.financeiro.repository.PessoaRepository;

@Service
public class PessoaService {
	
	@Autowired
	private PessoaRepository pessoaRepository; 
	
	@Autowired
	private UsuarioService usuarioService;
	
	public Page<Pessoa> pesquisar(PessoaFilter filtro, Pageable pageable){
		return this.pessoaRepository.filtrar(filtro, pageable);
	}
	
	public Pessoa salvar(Pessoa entidade) {
		Optional<Pessoa> pesquisarPorCpf = this.pessoaRepository.findByCpf(entidade.getCpf());
		if(pesquisarPorCpf.isPresent()) {
			throw new CustomRuntimeException("cpf já existe!");
		}
		if(entidade.getUsuario().getId() == 0) {
			Usuario usuarioSalvo = this.usuarioService.salvar(entidade.getUsuario());
			entidade.setUsuario(usuarioSalvo);
		}
		entidade.getContatos().forEach(c -> c.setPessoa(entidade));
		return this.pessoaRepository.save(entidade);
	}
	
	public Pessoa editar(Pessoa entidade) {
		Optional<Pessoa> entidadeSalva = buscarPorId(entidade.getId());
		Optional<Pessoa> pesquisarPorCpf = this.pessoaRepository.findByCpf(entidade.getCpf());
		if(!entidadeSalva.isPresent()) {
			throw new EmptyResultDataAccessException(1);
		}
		if(pesquisarPorCpf.isPresent() && entidadeSalva.get().getId() != entidade.getId()) {
			throw new CustomRuntimeException("cpf já existe!");	
		}
		if(entidadeSalva.get().getUsuario() != entidade.getUsuario()){
			Usuario usuarioEditado = this.usuarioService.editar(entidade.getUsuario());
			if(usuarioEditado != null) {
				entidade.setUsuario(usuarioEditado);
			}
		}
		entidade.getContatos().forEach(c -> c.setPessoa(entidade));
		BeanUtils.copyProperties(entidade, entidadeSalva, "id", "contatos");
		return this.pessoaRepository.save(entidade);
	}

	public Optional<Pessoa> buscarPorId(Long id) {
		return this.pessoaRepository.findById(id);
	}
	
	public void excluir(Long id) {
		Optional<Pessoa> entidadeSalva = this.pessoaRepository.findById(id);
		if(!entidadeSalva.isPresent()) {
			throw new EmptyResultDataAccessException(1);
		}
		this.pessoaRepository.deleteById(id);
	}
	
	public Iterable<Pessoa> listar() {
		return pessoaRepository.findAll();
	}
	
	public Optional<Pessoa> buscarUsuarioById(long idUsuario){
		return this.pessoaRepository.buscarUsuarioById(idUsuario);
	}
}

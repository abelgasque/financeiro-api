package com.br.financeiro.resource;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.br.financeiro.event.RecursoCriadoEvent;
import com.br.financeiro.exceptionhandler.ApiExeptionHandler.Erro;
import com.br.financeiro.model.Lancamento;
import com.br.financeiro.model.dto.Anexo;
import com.br.financeiro.model.dto.LancamentoEstatisticaCategoria;
import com.br.financeiro.model.dto.LancamentoEstatisticaDia;
import com.br.financeiro.model.dto.LancamentoEstatisticaMes;
import com.br.financeiro.model.dto.LancamentoEstatisticaPessoa;
import com.br.financeiro.model.filter.LancamentoFilter;
import com.br.financeiro.model.projection.ResumoLancamento;
import com.br.financeiro.repository.LancamentoRepository;
import com.br.financeiro.service.LancamentoService;
import com.br.financeiro.service.exception.PessoaInexistenteOuInativaException;
import com.br.financeiro.storage.S3;

import net.sf.jasperreports.engine.JRException;

@RestController
@RequestMapping("/lancamentos")
public class LancamentoResource {

	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private LancamentoService lancamentoService;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private S3 s3;
	
	
	@PreAuthorize("#oauth2.hasScope('write')")
	@RolesAllowed({ "ROLE_ADMINISTRADOR", "ROLE_PESSOA" })
	@PostMapping("/anexo")
	public Anexo uploadAnexo(@RequestParam MultipartFile anexo) throws IOException {
		String nome = s3.salvarTemporariamente(anexo);
		return new Anexo(nome, s3.configurarUrl(nome));
	}
	
	@PreAuthorize("#oauth2.hasScope('read')")
	@RolesAllowed({ "ROLE_ADMINISTRADOR", "ROLE_PESSOA" })
	@GetMapping("/relatorios/por-pessoa")
	public ResponseEntity<byte[]> relatorioPorPessoa(
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio, 
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fim
		) throws JRException{
		byte[] relatorio = this.lancamentoService.relatorioPorPessoa(inicio, fim);
		
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
				.body(relatorio);
	}
	
	@PreAuthorize("#oauth2.hasScope('read')")
	@RolesAllowed({ "ROLE_ADMINISTRADOR", "ROLE_PESSOA" })
	@GetMapping("/estatisticas/por-tipo-mensal")
	public List<LancamentoEstatisticaDia> estatisticasPorTipoMensal(){
		LocalDate mesReferencia = LocalDate.now();
		Long idPessoa = (long) 0;
		return this.lancamentoRepository.porDia(mesReferencia, idPessoa);
	}
	
	@PreAuthorize("#oauth2.hasScope('read')")
	@RolesAllowed({ "ROLE_ADMINISTRADOR", "ROLE_PESSOA" })
	@GetMapping("/estatisticas/por-mes/{anoReferencia}/{idPessoa}")
	public List<LancamentoEstatisticaMes> porAno(@PathVariable("anoReferencia") int anoReferencia, @PathVariable("idPessoa") Long idPessoa){
		return this.lancamentoService.estatisticasPorMes(anoReferencia, idPessoa);
	}
	
	@PreAuthorize("#oauth2.hasScope('read')")
	@RolesAllowed({ "ROLE_ADMINISTRADOR", "ROLE_PESSOA" })
	@GetMapping("/estatisticas/por-pessoa-by-id/{id}")
	public List<LancamentoEstatisticaPessoa> porPessoaById(@PathVariable("id") Long id){
		return this.lancamentoRepository.porPessoaById(id);
	}
	
	@PreAuthorize("#oauth2.hasScope('read')")
	@RolesAllowed({ "ROLE_ADMINISTRADOR", "ROLE_PESSOA" })
	@GetMapping("/estatisticas/por-categoria/{idPessoa}")
	public List<LancamentoEstatisticaCategoria> porCategoria(@PathVariable("idPessoa") Long id){
		return this.lancamentoRepository.porCategoria(LocalDate.now(), id);
	}
	
	@GetMapping(params = "resumo")
	@PreAuthorize("#oauth2.hasScope('read')")
	@RolesAllowed({ "ROLE_ADMINISTRADOR", "ROLE_PESSOA" })
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		return lancamentoRepository.resumir(lancamentoFilter, pageable);
	}
	
	@PreAuthorize("#oauth2.hasScope('write')")
	@RolesAllowed({ "ROLE_ADMINISTRADOR", "ROLE_PESSOA" })
	@PostMapping
	public ResponseEntity<?> salvar(@Valid @RequestBody Lancamento entidade, HttpServletResponse response) {
		Lancamento entidadeSalva = lancamentoService.salvar(entidade);
		publisher.publishEvent(new RecursoCriadoEvent(this, response, entidadeSalva.getId()));
		return ResponseEntity.status(HttpStatus.CREATED).body(entidadeSalva);
	}
	
	@PreAuthorize("#oauth2.hasScope('write')")
	@RolesAllowed({ "ROLE_ADMINISTRADOR", "ROLE_PESSOA" })
	@PutMapping
	public ResponseEntity<?> editar(@RequestBody Lancamento entidade){
		Lancamento entidadeSalva = this.lancamentoService.editar(entidade);	
		return new  ResponseEntity<Lancamento>(entidadeSalva,HttpStatus.OK);
	}
	
	@PreAuthorize("#oauth2.hasScope('read')")
	@RolesAllowed({ "ROLE_ADMINISTRADOR", "ROLE_PESSOA" })
	@GetMapping("/{id}")
	public ResponseEntity<?> buscar(@PathVariable("id") Long id) {
		 Optional<Lancamento> entidade = lancamentoService.buscarPorId(id);
		 return entidade != null ? ResponseEntity.ok(entidade) : ResponseEntity.notFound().build();
	}
	
	@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable("id") Long id){
		this.lancamentoService.excluir(id);
	}
	
	@ExceptionHandler({ PessoaInexistenteOuInativaException.class })
	public ResponseEntity<Object> handlePessoaInexistenteOuInativaException(PessoaInexistenteOuInativaException ex) {
		String mensagemUsuario = messageSource.getMessage("pessoa.inexistente-ou-inativa", null, LocaleContextHolder.getLocale());
		String mensagemDesenvolvedor = ex.toString();
		List<Erro> erros = Arrays.asList(new Erro(mensagemUsuario, mensagemDesenvolvedor));
		return ResponseEntity.badRequest().body(erros);
	}
}

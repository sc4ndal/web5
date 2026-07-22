package net.datasa.web5.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datasa.web5.domain.dto.BoardDTO;
import net.datasa.web5.domain.entity.BoardEntity;
import net.datasa.web5.domain.entity.MemberEntity;
import net.datasa.web5.exception.FileStorageException;
import net.datasa.web5.repository.BoardLikeRepository;
import net.datasa.web5.repository.BoardRepository;
import net.datasa.web5.repository.MemberRespository;
import net.datasa.web5.repository.ReplyRepository;
import net.datasa.web5.util.FileManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/*
	게시판 관련 서비스
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BoardService {
	
	private final MemberRespository 	mr;
	private final BoardRepository 		br;
	private final ReplyRepository 		rr;
	private final BoardLikeRepository 	blr;
	private final FileManager 			fileManager;
	
	//------------------------------------------------------------------------------
	
	/**
	 * 게시글 저장
	 * @param boardDTO		저장할 글 정보
	 * @param uploadPath	파일 저장할 경로
	 * @param upload		업로드한 파일
	 */
	public void write(BoardDTO boardDTO, String uploadPath, MultipartFile upload) {
		
		MemberEntity memberEntity = mr.findById(boardDTO.getMemberId()).orElseThrow(() -> new EntityNotFoundException("회원이 없습니다."));
		
		String fileName = null;
		String originalName = null;
		
		if (upload != null && !upload.isEmpty()) {
			try {
				fileName = fileManager.saveFile(uploadPath, upload);
				originalName = upload.getOriginalFilename();
			} catch (IOException e) {
				// checked 예외인 IOException을 런타임 예외 종류로 전환
				throw new FileStorageException("파일이 없습니다.");
			}
		}
		
		BoardEntity entity = BoardEntity.builder()
				.member(memberEntity)
				.title(boardDTO.getTitle())
				.contents(boardDTO.getContents())
				.fileName(fileName)
				.originalName(originalName)
				.build();
		log.debug("저장되는 게시글 정보: {}", entity);
		br.save(entity);
		
	}
}

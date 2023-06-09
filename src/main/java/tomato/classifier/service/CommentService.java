package tomato.classifier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tomato.classifier.auth.SessionConst;
import tomato.classifier.dto.CommentDto;
import tomato.classifier.entity.Article;
import tomato.classifier.entity.Comment;
import tomato.classifier.entity.User;
import tomato.classifier.repository.ArticleRepository;
import tomato.classifier.repository.CommentRepository;
import tomato.classifier.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final ArticleRepository articleRepository;

    private final CommentRepository commentRepository;

    private final UserRepository userRepository;

    public List<CommentDto> comments(Integer articleId) {

        List<Comment> allComments = commentRepository.findByArticleId(articleId);

        List<CommentDto> allCommentDtos = allComments
                .stream()
                .map(comment -> CommentDto.convertDto(comment))
                .collect(Collectors.toList());

        List<CommentDto> comments = new ArrayList<>();

        allCommentDtos.stream()
                .filter(commentDto -> commentDto.isDeleteYn() == false)
                .forEach(commentDto -> comments.add(commentDto));

        return comments;
    }

    @Transactional
    public CommentDto create(int articleId, CommentDto commentDto) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 생성 실패, 게시글 조회 실패"));

        commentDto.setDeleteYn(false);
        commentDto.setUpdateYn(false);

        String nickname = commentDto.getNickname();

        User user = userRepository.findByNickname(nickname);

        Comment comment = Comment.convertEntity(commentDto, article, user);

        Comment created = commentRepository.save(comment);

        return CommentDto.convertDto(created);
    }

    @Transactional
    public CommentDto update(Integer commentId, CommentDto commentDto) {


        Comment target = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 조회 실패!"));

        target.patch(commentDto);

        Comment updated = commentRepository.save(target);

        return CommentDto.convertDto(updated);
    }

    @Transactional
    public CommentDto delete(Integer commentId) {

        Comment target = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 조회 실패!"));

        target.delete();

        Comment deleted = commentRepository.save(target);

        return CommentDto.convertDto(deleted);
    }
}

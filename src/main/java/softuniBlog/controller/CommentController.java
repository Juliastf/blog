package softuniBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import softuniBlog.bindingModel.ArticleBindingModel;
import softuniBlog.bindingModel.CommentBindingModel;
import softuniBlog.entity.Article;
import softuniBlog.entity.Comment;
import softuniBlog.entity.User;
import softuniBlog.repository.ArticleRepository;
import softuniBlog.repository.CommentRepository;
import softuniBlog.repository.UserRepository;

@Controller
public class CommentController {
    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepositoryRepository;

    @Autowired
    private CommentRepository commentRepository;


    @PostMapping("/article/{id}/post_comment")
    public String postComment(@PathVariable Integer id, CommentBindingModel commentBindingModel) {
        if (!this.articleRepository.exists(id)) {
            return "redirect:/";
        }

        Article article = this.articleRepository.findOne(id);

        Comment comment = new Comment(
                commentBindingModel.getName(),
                commentBindingModel.getText(),
                article
        );

        this.commentRepository.saveAndFlush(comment);

        return "redirect:/article/" + id;
    }

    private boolean isUserAdmin() {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity = this.userRepositoryRepository.findByEmail(user.getUsername());

        return userEntity.isAdmin();
    }

    @GetMapping("/comment/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteCommentView(@PathVariable Integer id, Model model) {
        if (!this.commentRepository.exists(id)) {
            return "redirect:/";
        }

        Comment comment = this.commentRepository.findOne(id);

        if (!isUserAdmin()) {
            return "redirect:/";
        }

        model.addAttribute("comment", comment);
        model.addAttribute("view", "comment/delete");

        return "base-layout";

    }

        @PreAuthorize("isAuthenticated()")
        @PostMapping("/comment/delete/{id}")
        public String deleteCommentProcess (@PathVariable Integer id){
            if (!this.commentRepository.exists(id)) {
                return "redirect:/";
            }

            if (!isUserAdmin()) {
                return "redirect:/";
            }

            Comment comment = this.commentRepository.findOne(id);

            this.commentRepository.delete(comment);

            return "redirect:/article/" + comment.getArticleId().getId();
        }
    }


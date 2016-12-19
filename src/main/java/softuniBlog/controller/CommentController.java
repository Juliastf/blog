package softuniBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import softuniBlog.bindingModel.ArticleBindingModel;
import softuniBlog.bindingModel.CommentBindingModel;
import softuniBlog.entity.Article;
import softuniBlog.entity.Comment;
import softuniBlog.repository.ArticleRepository;
import softuniBlog.repository.CommentRepository;

@Controller
public class CommentController {
    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @PostMapping("/article/{id}/post_comment")
    public String postComment(@PathVariable Integer id, CommentBindingModel commentBindingModel){
        if (!this.articleRepository.exists(id)) {
            return "redirect:/";
        }

        Article article=this.articleRepository.findOne(id);

        Comment comment=new Comment(
                commentBindingModel.getName(),
                commentBindingModel.getText(),
                article
        );

        this.commentRepository.saveAndFlush(comment);

        return "redirect:/article/"+id;
    }
}

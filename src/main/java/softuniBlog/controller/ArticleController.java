package softuniBlog.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import softuniBlog.bindingModel.ArticleBindingModel;
import softuniBlog.bindingModel.FileBindingModel;
import softuniBlog.entity.*;
import softuniBlog.repository.*;


import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;


@Controller
public class ArticleController {
    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/article/create")
    @PreAuthorize("isAuthenticated()")
    public String create(Model model) {
        model.addAttribute("view", "article/create");
        List<Category> categories=this.categoryRepository.findAll();
        model.addAttribute("categories", categories);

        return "base-layout";


    }

    @PostMapping("/article/create")
    @PreAuthorize("isAuthenticated()")
    public String createProcess(ArticleBindingModel articleBindingModel,
                                FileBindingModel fileBindingModel, RedirectAttributes redirectAttributes){
        if (StringUtils.isEmpty(articleBindingModel.getTitle())) {
            redirectAttributes.addFlashAttribute("error", "The Title can not be empty!");
            return "redirect:/article/create";}
        if (StringUtils.isEmpty(articleBindingModel.getContent())) {
            redirectAttributes.addFlashAttribute("error", "The Content can not be empty!");
            return "redirect:/article/create";}

        UserDetails user=(UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();



        User userEntity = this.userRepository.findByEmail(user.getUsername());
        Category category=this.categoryRepository.findOne(articleBindingModel.getCategoryId());
        HashSet<Tag> tags=this.findTagsFromString(articleBindingModel.getTagString());

        Article articleEntity=new Article(
                articleBindingModel.getTitle(),
                articleBindingModel.getContent(),
                userEntity,
                category,
                tags
        );

        MultipartFile file=fileBindingModel.getPicture();
        if (file!=null) {
            String originalFilename=file.getOriginalFilename();

            File imageFile=new File
                    ("C:\\Users\\nikijul\\Desktop\\Project\\blog\\src\\main\\resources\\static\\images\\", originalFilename);
            try {
                file.transferTo(imageFile);
                Integer index = imageFile.getPath().lastIndexOf("\\");
                String dbpath=imageFile.getPath().substring(index + 1);
                articleEntity.setImagePath(dbpath);
                this.articleRepository.saveAndFlush(articleEntity);
            } catch(IOException e) {
                e.printStackTrace();

            }
        }



        return "redirect:/";

    }

    @GetMapping("/article/{id}")
    public String details(Model model, @PathVariable Integer id){
        if (!this.articleRepository.exists(id)) {
            return "redirect:/";
        }
        if (!(SecurityContextHolder.getContext().getAuthentication()
        instanceof AnonymousAuthenticationToken)){
            UserDetails principal=(UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();

            User entityUser=this.userRepository.findByEmail(principal.getUsername());
            model.addAttribute("user", entityUser);

        }
        Article article=this.articleRepository.findOne(id);
        List<Comment> comments = article.getComments().stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .collect(Collectors.toList());

        model.addAttribute("article", article);
        model.addAttribute("view", "article/details");
        model.addAttribute("comments", comments);

        return "base-layout";
    }

    @GetMapping("/article/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Integer id, Model model) {
        if (!this.articleRepository.exists(id)) {
            return "redirect:/";
        }
        Article article=this.articleRepository.findOne(id);

        if (!isUserAuthorOrAdmin(article)) {
            return "redirect:/article/"+id;
        }
        List<Category> categories=this.categoryRepository.findAll();
        String tagString=article.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));

        model.addAttribute("view", "article/edit");
        model.addAttribute("article", article);
        model.addAttribute("categories",categories);
        model.addAttribute("tags", tagString);

        return "base-layout";


    }

    @PostMapping("/article/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProcess(@PathVariable Integer id, ArticleBindingModel articleBindingModel, FileBindingModel fileBindingModel ) {
        if (!this.articleRepository.exists(id)) {
            return "redirect:/";
        }
        Article article=this.articleRepository.findOne(id);

        if (!isUserAuthorOrAdmin(article)) {
            return "redirect:/article/"+id;
        }

        Category category=this.categoryRepository.findOne(articleBindingModel.getCategoryId());
        HashSet<Tag> tags=this.findTagsFromString(articleBindingModel.getTagString());

        article.setCategory(category);
        article.setContent(articleBindingModel.getContent());
        article.setTitle(articleBindingModel.getTitle());
        article.setTags(tags);


        MultipartFile fileEdit=fileBindingModel.getPicture();
        if (fileEdit!=null) {
            String originalFilename=fileEdit.getOriginalFilename();

            File editImageFile=new File
                    ("C:\\Users\\nikijul\\Desktop\\Project\\blog\\src\\main\\resources\\static\\images\\", originalFilename);
            try {
                fileEdit.transferTo(editImageFile);
                Integer indexEdit = editImageFile.getPath().lastIndexOf("\\");
                String dbpathEdit=editImageFile.getPath().substring(indexEdit + 1);
                article.setImagePath(dbpathEdit);
                this.articleRepository.saveAndFlush(article);
            } catch(IOException e) {
                e.printStackTrace();

            }
        }

        return "redirect:/article/" + article.getId();
    }

    @GetMapping("/article/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable Integer id, Model model) {
        if (!this.articleRepository.exists(id)) {
            return "redirect:/";
        }
        Article article = this.articleRepository.findOne(id);

        if (!isUserAuthorOrAdmin(article)) {
            return "redirect:/article/"+id;
        }

        model.addAttribute("article", article);
        model.addAttribute("view", "article/delete");

        return "base-layout";
    }

    @PostMapping("/article/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProcess(@PathVariable Integer id) {
        if (!this.articleRepository.exists(id)) {
            return "redirect:/";
        }
        Article article = this.articleRepository.findOne(id);

        if (!isUserAuthorOrAdmin(article)) {
            return "redirect:/article/"+id;
        }
        for (Comment comment:article.getComments()) {
            this.commentRepository.delete(comment);
        }

        this.articleRepository.delete(article);

        return "redirect:/";
    }

    private boolean isUserAuthorOrAdmin(Article article) {
        UserDetails user=(UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity=this.userRepository.findByEmail(user.getUsername());

        return userEntity.isAdmin()||userEntity.isAuthor(article);
    }

    private HashSet<Tag> findTagsFromString(String tagString) {
        HashSet<Tag> tags = new HashSet<>();
        String[] tagNames = tagString.split(",\\s*") ;
        for (String tagName:tagNames) {
            Tag currentTag= this.tagRepository.findByName(tagName);
            if (currentTag==null){
                currentTag=new Tag(tagName);
                this.tagRepository.saveAndFlush(currentTag);
            }
            tags.add(currentTag);
        }
        return  tags;

    }

}

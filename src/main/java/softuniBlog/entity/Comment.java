package softuniBlog.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table (name="comments")
public class Comment {
    private Integer id;

    private String name;

    private String text;

    private Article articleId;

    private Date date;

    public Comment(){

    }

    public Comment(String name, String text, Article articleId) {
        this.name=name;
        this.text=text;
        this.articleId=articleId;
        this.date=new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(nullable = false, columnDefinition = "text")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @ManyToOne
    @JoinColumn(nullable = false, name = "articleId")
    public Article getArticleId() {
        return articleId;
    }

    public void setArticleId(Article articleId) {
        this.articleId = articleId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

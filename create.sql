
CREATE TABLE answers (
            question_id INT NOT NULL,
            answer_id INT NOT NULL,
            creation_date INT,
            body MEDIUMTEXT,
            score INT,
            PRIMARY KEY (answer_id));

CREATE TABLE qcomments (
            comment_id INT NOT NULL,
            question_id INT NOT NULL,
            creation_date INT,
            body MEDIUMTEXT,
            score INT);

CREATE TABLE acomments (
            comment_id INT NOT NULL,
            answer_id INT NOT NULL,
            creation_date INT,
            body MEDIUMTEXT,
            score INT);

CREATE TABLE questions (
            question_id INT NOT NULL,
            creation_date INT,
            title VARCHAR(255),
            body MEDIUMTEXT,
            view_count INT,
            last_edit_date INT,
            last_activity_date INT,
            link VARCHAR(255),
            score INT,
            PRIMARY KEY (question_id));

CREATE TABLE tags (
       question_id INT NOT NULL,
       tag VARCHAR(255));

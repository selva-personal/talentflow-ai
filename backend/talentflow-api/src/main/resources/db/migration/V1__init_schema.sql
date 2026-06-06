-- TalentFlow AI - PostgreSQL Schema

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    role_id         BIGINT NOT NULL REFERENCES roles(id),
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    reset_token     VARCHAR(255),
    reset_token_exp TIMESTAMPTZ,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role_id);

CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

CREATE TABLE resumes (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name       VARCHAR(255) NOT NULL,
    file_path       VARCHAR(500) NOT NULL,
    file_size       BIGINT NOT NULL,
    mime_type       VARCHAR(100) NOT NULL DEFAULT 'application/pdf',
    extracted_text  TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_resumes_user ON resumes(user_id);

CREATE TABLE resume_analysis (
    id              BIGSERIAL PRIMARY KEY,
    resume_id       BIGINT NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ats_score       INTEGER NOT NULL CHECK (ats_score >= 0 AND ats_score <= 100),
    strengths       JSONB NOT NULL DEFAULT '[]',
    weaknesses      JSONB NOT NULL DEFAULT '[]',
    missing_skills  JSONB NOT NULL DEFAULT '[]',
    recommendations JSONB NOT NULL DEFAULT '[]',
    raw_ai_response JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_resume_analysis_user ON resume_analysis(user_id);
CREATE INDEX idx_resume_analysis_resume ON resume_analysis(resume_id);

CREATE TABLE interviews (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    role_target     VARCHAR(150) NOT NULL,
    experience_level VARCHAR(50) NOT NULL,
    skill_level     VARCHAR(50) NOT NULL,
    interview_type  VARCHAR(50) NOT NULL DEFAULT 'MIXED',
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    overall_score   INTEGER CHECK (overall_score IS NULL OR (overall_score >= 0 AND overall_score <= 100)),
    feedback        TEXT,
    improvements    JSONB DEFAULT '[]',
    session_data    JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_interviews_user ON interviews(user_id);
CREATE INDEX idx_interviews_status ON interviews(status);

CREATE TABLE interview_questions (
    id              BIGSERIAL PRIMARY KEY,
    interview_id    BIGINT NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    question_type   VARCHAR(50) NOT NULL,
    question_text   TEXT NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_interview_questions_interview ON interview_questions(interview_id);

CREATE TABLE interview_answers (
    id              BIGSERIAL PRIMARY KEY,
    question_id     BIGINT NOT NULL REFERENCES interview_questions(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    answer_text     TEXT NOT NULL,
    score           INTEGER CHECK (score IS NULL OR (score >= 0 AND score <= 100)),
    feedback        TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_interview_answers_question ON interview_answers(question_id);

CREATE TABLE mock_interview_sessions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    role_target     VARCHAR(150),
    status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    messages        JSONB NOT NULL DEFAULT '[]',
    progress_percent INTEGER NOT NULL DEFAULT 0 CHECK (progress_percent >= 0 AND progress_percent <= 100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mock_sessions_user ON mock_interview_sessions(user_id);

CREATE TABLE coding_tests (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    language        VARCHAR(30) NOT NULL,
    problem_statement TEXT NOT NULL,
    starter_code    TEXT,
    difficulty      VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_coding_tests_user ON coding_tests(user_id);

CREATE TABLE coding_submissions (
    id              BIGSERIAL PRIMARY KEY,
    test_id         BIGINT NOT NULL REFERENCES coding_tests(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code            TEXT NOT NULL,
    output          TEXT,
    passed          BOOLEAN,
    ai_score        INTEGER CHECK (ai_score IS NULL OR (ai_score >= 0 AND ai_score <= 100)),
    complexity_analysis TEXT,
    suggestions     JSONB DEFAULT '[]',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_coding_submissions_test ON coding_submissions(test_id);
CREATE INDEX idx_coding_submissions_user ON coding_submissions(user_id);

CREATE TABLE career_roadmaps (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    "current_role"  VARCHAR(150) NOT NULL,
    "target_role"   VARCHAR(150) NOT NULL,
    timeline_months INTEGER NOT NULL,
    roadmap_data    JSONB NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_career_roadmaps_user ON career_roadmaps(user_id);

CREATE TABLE cover_letters (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_title       VARCHAR(150) NOT NULL,
    company_name    VARCHAR(150) NOT NULL,
    content         TEXT NOT NULL,
    tone            VARCHAR(50) NOT NULL DEFAULT 'PROFESSIONAL',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cover_letters_user ON cover_letters(user_id);

CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    message         TEXT NOT NULL,
    type            VARCHAR(50) NOT NULL DEFAULT 'INFO',
    read            BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id, read) WHERE read = FALSE;

CREATE TABLE activity_logs (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(50),
    entity_id       BIGINT,
    metadata        JSONB,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_logs_user ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_created ON activity_logs(created_at DESC);

INSERT INTO roles (name, description) VALUES
    ('USER', 'Standard platform user'),
    ('ADMIN', 'Administrator with elevated privileges');

\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{zenbuddy-report}[2026/04/21 ZenBuddy scientific report class]

\LoadClass[12pt,a4paper,oneside]{report}

\RequirePackage{iftex}
\ifPDFTeX
  \ClassError{zenbuddy-report}{Please compile this document with XeLaTeX or LuaLaTeX}{%
    The report contains Vietnamese text and relies on Unicode font support.%
  }
\fi

\RequirePackage{fontspec}
\RequirePackage{polyglossia}
\setdefaultlanguage{vietnamese}
\setmainfont{TeX Gyre Termes}
\setsansfont{TeX Gyre Heros}
\setmonofont{Latin Modern Mono}

\RequirePackage{geometry}
\geometry{
  top=2.5cm,
  bottom=2.5cm,
  left=3cm,
  right=2.5cm,
  headheight=15pt,
  footskip=1.2cm
}

\RequirePackage{setspace}
\onehalfspacing

\RequirePackage{amsmath}
\RequirePackage{amssymb}
\RequirePackage{array}
\RequirePackage{booktabs}
\RequirePackage{caption}
\RequirePackage{enumitem}
\RequirePackage{float}
\RequirePackage{fancyhdr}
\RequirePackage{graphicx}
\RequirePackage{hyperref}
\RequirePackage{longtable}
\RequirePackage{multirow}
\RequirePackage{tabularx}
\RequirePackage{titlesec}
\RequirePackage{xcolor}
\RequirePackage{listings}
\RequirePackage{csquotes}
\RequirePackage{tcolorbox}

\definecolor{zbPrimary}{HTML}{5B4B8A}
\definecolor{zbSecondary}{HTML}{E9F4F1}
\definecolor{zbAccent}{HTML}{E77C52}
\definecolor{zbBorder}{HTML}{D0D6E2}
\definecolor{zbCodeBg}{HTML}{F6F8FC}
\definecolor{zbCodeComment}{HTML}{5B6B7A}
\definecolor{zbCodeKeyword}{HTML}{7A3E9D}
\definecolor{zbCodeString}{HTML}{00796B}
\definecolor{zbLink}{HTML}{1E5AA8}

\hypersetup{
  colorlinks=true,
  linkcolor=zbPrimary,
  citecolor=zbAccent,
  urlcolor=zbLink,
  pdfauthor={ZenBuddy Report Template},
  pdftitle={ZenBuddy Scientific Report}
}

\setlength{\parindent}{1.25cm}
\setlength{\parskip}{0.35em}
\setcounter{secnumdepth}{3}
\setcounter{tocdepth}{2}
\setlist[itemize]{leftmargin=2em}
\setlist[enumerate]{leftmargin=2.2em}

\pagestyle{fancy}
\fancyhf{}
\fancyhead[L]{\nouppercase{\leftmark}}
\fancyhead[R]{ZenBuddy}
\fancyfoot[C]{\thepage}
\renewcommand{\headrulewidth}{0.4pt}
\renewcommand{\footrulewidth}{0pt}

\titleformat{\chapter}[display]
  {\bfseries\Huge}
  {\filright\color{zbPrimary}\chaptername\ \thechapter}
  {1ex}
  {\filright}

\titlespacing*{\chapter}{0pt}{-10pt}{24pt}
\titleformat{\section}{\Large\bfseries\color{zbPrimary}}{\thesection}{0.75em}{}
\titleformat{\subsection}{\large\bfseries}{\thesubsection}{0.75em}{}
\titleformat{\subsubsection}{\normalsize\bfseries}{\thesubsubsection}{0.75em}{}

\captionsetup{
  font=small,
  labelfont=bf,
  labelsep=period
}

\lstdefinelanguage{Kotlin}{
  morekeywords={
    package,import,class,interface,object,data,sealed,open,private,public,internal,
    protected,fun,val,var,if,else,when,for,while,do,return,break,continue,try,catch,
    finally,throw,null,true,false,in,is,this,super,override,suspend,operator,companion,
    by,get,set,typealias,where
  },
  sensitive=true,
  morecomment=[l]{//},
  morecomment=[s]{/*}{*/},
  morestring=[b]",
}

\lstdefinestyle{zenbuddy}{
  backgroundcolor=\color{zbCodeBg},
  basicstyle=\ttfamily\small,
  breaklines=true,
  captionpos=b,
  commentstyle=\color{zbCodeComment}\itshape,
  frame=single,
  framerule=0.25pt,
  keywordstyle=\color{zbCodeKeyword}\bfseries,
  numberstyle=\tiny\color{zbCodeComment},
  numbers=left,
  numbersep=8pt,
  rulecolor=\color{zbBorder},
  showstringspaces=false,
  stringstyle=\color{zbCodeString},
  tabsize=2
}

\newtcolorbox{reportnote}{
  colback=zbSecondary,
  colframe=zbBorder,
  boxrule=0.5pt,
  arc=2mm,
  left=1em,
  right=1em,
  top=0.8em,
  bottom=0.8em
}

\makeatletter
\def\@reporttitle{BÁO CÁO ĐỒ ÁN}
\def\@reportsubtitle{}
\def\@reportauthor{[Điền họ tên]}
\def\@reportstudentid{[Điền mã số sinh viên]}
\def\@reportadvisor{[Điền tên giảng viên hướng dẫn]}
\def\@reportfaculty{[Điền khoa]}
\def\@reportdepartment{[Điền bộ môn]}
\def\@reportuniversity{[Điền tên trường]}
\def\@reportlocation{[Điền địa điểm]}
\def\@reportdate{\today}
\def\@reporttype{Báo cáo đồ án môn học}

\newcommand{\reporttitle}[1]{\gdef\@reporttitle{#1}}
\newcommand{\reportsubtitle}[1]{\gdef\@reportsubtitle{#1}}
\newcommand{\reportauthor}[1]{\gdef\@reportauthor{#1}}
\newcommand{\reportstudentid}[1]{\gdef\@reportstudentid{#1}}
\newcommand{\reportadvisor}[1]{\gdef\@reportadvisor{#1}}
\newcommand{\reportfaculty}[1]{\gdef\@reportfaculty{#1}}
\newcommand{\reportdepartment}[1]{\gdef\@reportdepartment{#1}}
\newcommand{\reportuniversity}[1]{\gdef\@reportuniversity{#1}}
\newcommand{\reportlocation}[1]{\gdef\@reportlocation{#1}}
\newcommand{\reportdate}[1]{\gdef\@reportdate{#1}}
\newcommand{\reporttype}[1]{\gdef\@reporttype{#1}}

\newcommand{\makereporttitle}{%
  \begin{titlepage}
    \centering
    \vspace*{1.5cm}
    {\Large\bfseries \@reportuniversity\par}
    \vspace{0.5cm}
    {\large \@reportfaculty\par}
    {\large \@reportdepartment\par}
    \vfill
    {\Large\bfseries \@reporttype\par}
    \vspace{1cm}
    {\huge\bfseries \@reporttitle\par}
    \ifx\@reportsubtitle\@empty\else
      \vspace{0.75cm}
      {\Large \@reportsubtitle\par}
    \fi
    \vfill
    \begin{tabular}{>{\bfseries}p{4.5cm}p{8.5cm}}
      Sinh viên thực hiện: & \@reportauthor \\
      Mã số sinh viên: & \@reportstudentid \\
      Giảng viên hướng dẫn: & \@reportadvisor \\
    \end{tabular}
    \vfill
    {\large \@reportlocation, \@reportdate\par}
  \end{titlepage}
}
\makeatother

\newcommand{\placeholderfigure}[4][0.82\textwidth]{%
  \begin{figure}[H]
    \centering
    \fbox{%
      \begin{minipage}[c][0.25\textheight][c]{#1}
        \centering
        {\Large\bfseries Placeholder hình ảnh}\\[0.75em]
        {\bfseries #2}\\[0.75em]
        \small #4
      \end{minipage}%
    }
    \caption{#2}
    \label{#3}
  \end{figure}
}

\newcommand{\codeplaceholder}[3]{%
  \begin{figure}[H]
    \centering
    \fbox{%
      \begin{minipage}[c][0.16\textheight][c]{0.9\textwidth}
        \centering
        {\large\bfseries Placeholder khối mã}\\[0.5em]
        \textbf{#1}\\[0.5em]
        \small #3
      \end{minipage}%
    }
    \caption{#1}
    \label{#2}
  \end{figure}
}

\newcommand{\appendixsection}[1]{%
  \chapter*{#1}
  \addcontentsline{toc}{chapter}{#1}
}

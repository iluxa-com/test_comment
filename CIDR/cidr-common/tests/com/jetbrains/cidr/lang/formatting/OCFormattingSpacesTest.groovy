package com.jetbrains.cidr.lang.formatting

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.jetbrains.cidr.lang.psi.OCFile

public class OCFormattingSpacesTest extends OCFormattingTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    getCommonSettings().SPECIAL_ELSE_IF_TREATMENT = true;
  }

  public void testFileLevelSelectionFormat() throws Exception {
    // format empty file with OCFile-level selection - the conner case for refactoring tools
    final OCFile file = configureByTextOC(" ");
    WriteCommandAction.runWriteCommandAction(getProject(), new Runnable() {
      @Override
      public void run() {
        CodeStyleManager.getInstance(getProject()).reformatText(file, 0, 1);
      }
    });
  }

  public void testObjCKeywordsAsFunction() throws Exception {
    prepareForFormat(
      """void  foo  (  )  {}
        |@implementation Foo
        |- (BOOL)validateMenuItem:(NSMenuItem *)item {
        |    foo  (  )  ;
        |    @  selector (  );
        |    if ([item action] == @  selector  (  nextRecord  )  ) {}
        |    char *buf1 = @  encode  (  int **  )  ;
        |    char *buf2 = @   encode   (   struct key   )   ;
        |    char *buf3 = @    encode    (    Rectangle    )    ;
        |    return YES;
        |}
        |@end""".stripMargin());

    getOCSettings().SPACE_WITHIN_FUNCTION_CALL_PARENTHESES = false;
    getOCSettings().SPACE_WITHIN_EMPTY_FUNCTION_CALL_PARENTHESES = true;
    getCommonSettings().SPACE_BEFORE_METHOD_CALL_PARENTHESES = false;
    assertFormat(
      """void foo(){
        |}
        |@implementation Foo
        |-(BOOL)validateMenuItem:(NSMenuItem*)item{
        |    foo( );
        |    @selector( );
        |    if([item action]==@selector(nextRecord)){
        |    }
        |    char*buf1=@encode(int**);
        |    char*buf2=@encode(struct key);
        |    char*buf3=@encode(Rectangle);
        |    return YES;
        |}
        |@end""".stripMargin());

    getOCSettings().SPACE_WITHIN_FUNCTION_CALL_PARENTHESES = true;
    getOCSettings().SPACE_WITHIN_EMPTY_FUNCTION_CALL_PARENTHESES = false;
    getCommonSettings().SPACE_BEFORE_METHOD_CALL_PARENTHESES = true;
    assertFormat(
      """void foo(){
        |}
        |@implementation Foo
        |-(BOOL)validateMenuItem:(NSMenuItem*)item{
        |    foo ();
        |    @selector ();
        |    if([item action]==@selector ( nextRecord )){
        |    }
        |    char*buf1=@encode ( int** );
        |    char*buf2=@encode ( struct key );
        |    char*buf3=@encode ( Rectangle );
        |    return YES;
        |}
        |@end""".stripMargin());
  }

  public void testTrimmingExtraSpaces() throws Exception {
    assertFormat("  @class  Foo  ,  Bar;  \n" +
                 "  @protocol  Foo  ,  Bar;  \n" +
                 "  @class  Foo  \n" +
                 "  @end  \n" +
                 "  @interface  Foo{  \n" +
                 "  }  \n" +
                 "  @end  \n" +
                 "  @implementation  Foo  \n" +
                 "  @synthesize  p1;  \n" +
                 "  @end  \n" +
                 "  void  foo();  \n" +
                 "  void  foo(){  \n" +
                 "  }\n" +
                 "  typedef  struct   foo   {\n" +
                 "  }  foo_t;",

                 "@class Foo,Bar;\n" +
                 "@protocol Foo,Bar;\n" +
                 "@class Foo\n" +
                 "@end\n" +
                 "@interface Foo{\n" +
                 "}\n" +
                 "@end\n" +
                 "@implementation Foo\n" +
                 "@synthesize p1;\n" +
                 "@end\n" +
                 "void foo();\n" +
                 "void foo(){\n" +
                 "}\n" +
                 "typedef struct foo{\n" +
                 "}foo_t;");
  }

  public void testTrimmingExtraSpacesAfterReturn() throws Exception {
    assertFormat("void foo(){\n" +
                 "    return  1;\n" +
                 "    return  ;\n" +
                 "}\n",

                 "void foo(){\n" +
                 "    return 1;\n" +
                 "    return;\n" +
                 "}\n");
  }

  public void testTrimmingExtraSpacesInSendMessage() throws Exception {
    assertFormat("void foo(){\n" +
                 "    [  [  foo  bar  ]  baz  ];\n" +
                 "}\n",

                 "void foo(){\n" +
                 "    [[foo bar]baz];\n" +
                 "}\n");
  }

  public void testTrimmingExtraSpacesAroundTypeModifiers() throws Exception {
    assertFormat("void foo(){\n" +
                 "    const    int    i;\n" +
                 "    int    const    i;\n" +
                 "    int    const    *    i;\n" +
                 "    int    *    const   i;\n" +
                 "    const    int    const    i;\n" +
                 "    const    int    const    *    const    i;\n" +
                 "    volatile   int i;\n" +
                 "    long    long    int i;\n" +
                 "    unsigned    int i;\n" +
                 "    static     int i;\n" +
                 "}\n" +
                 "extern   \"C\"   const   int  foo();\n" +
                 "extern   \"C\"   const   int  foo(){\n" +
                 "}\n" +
                 "extern   \"C\"   const   int  foo;\n" +
                 "extern   const   int   foo;\n" +
                 "static   const  int  *foo();\n" +
                 "static   const  int  *foo(){\n" +
                 "}\n" +
                 "const   long   int   *   const   *   const   x  (  const   int  *   const   x  );",

                 "void foo(){\n" +
                 "    const int i;\n" +
                 "    int const i;\n" +
                 "    int const*i;\n" +
                 "    int*const i;\n" +
                 "    const int const i;\n" +
                 "    const int const*const i;\n" +
                 "    volatile int i;\n" +
                 "    long long int i;\n" +
                 "    unsigned int i;\n" +
                 "    static int i;\n" +
                 "}\n" +
                 "extern \"C\" const int foo();\n" +
                 "extern \"C\" const int foo(){\n" +
                 "}\n" +
                 "extern \"C\" const int foo;\n" +
                 "extern const int foo;\n" +
                 "static const int*foo();\n" +
                 "static const int*foo(){\n" +
                 "}\n" +
                 "const long int*const*const x(const int*const x);");
  }

  public void testTrimmingExtraSpacesBeforeFunctionModifierInCPP() throws Exception {
    assertFormatCPP("class Foo{\n" +
                    "    void    foo()   const;\n" +
                    "    friend    void    foo();\n" +
                    "    friend    class   Bar;\n" +
                    "};\n",

                    "class Foo{\n" +
                    "    void foo() const;\n" +
                    "    friend void foo();\n" +
                    "    friend class Bar;\n" +
                    "};\n");
  }

  public void testTrimmingExtraSpacesInCPP() throws Exception {
    assertFormatCPP("  class  Foo  {\n" +
                    " };\n",

                    "class Foo{\n" +
                    "};\n");
  }

  public void testKeyworsWithArgList() throws Exception {
    prepareForFormatCPP(
      """class A{};
        |void bar() noexcept(true);
        |void car() throw();
        |void foo() throw(A){
        |   alignas(16) int a[4];
        |   static_assert(alignof(a) == 16);
        |   static_assert(sizeof(a) % 16 == 0);
        |   auto f(int t) -> decltype(t){return t;}
        |   auto g(int t) -> typeof(t){return t;}
        |   A aa;
        |   typeid(aa).name();
        |}""".stripMargin());

    assertFormat(
      """class A{
        |};
        |void bar() noexcept(true);
        |void car() throw();
        |void foo() throw(A){
        |    alignas(16) int a[4];
        |    static_assert(alignof(a)==16);
        |    static_assert(sizeof(a)%16==0);
        |    auto f(int t)->decltype(t){
        |        return t;
        |    }
        |    auto g(int t)->typeof(t){
        |        return t;
        |    }
        |    A aa;
        |    typeid(aa).name();
        |}""".stripMargin());

    getCommonSettings().SPACE_BEFORE_METHOD_CALL_PARENTHESES = true;
    assertFormat(
      """class A{
        |};
        |void bar() noexcept (true);
        |void car() throw ();
        |void foo() throw (A){
        |    alignas (16) int a[4];
        |    static_assert (alignof (a)==16);
        |    static_assert (sizeof (a)%16==0);
        |    auto f(int t)->decltype (t){
        |        return t;
        |    }
        |    auto g(int t)->typeof (t){
        |        return t;
        |    }
        |    A aa;
        |    typeid (aa).name ();
        |}""".stripMargin());

    getOCSettings().SPACE_WITHIN_FUNCTION_CALL_PARENTHESES = true;
    assertFormat(
      """class A{
        |};
        |void bar() noexcept ( true );
        |void car() throw ();
        |void foo() throw ( A ){
        |    alignas ( 16 ) int a[4];
        |    static_assert ( alignof ( a )==16 );
        |    static_assert ( sizeof ( a )%16==0 );
        |    auto f(int t)->decltype ( t ){
        |        return t;
        |    }
        |    auto g(int t)->typeof ( t ){
        |        return t;
        |    }
        |    A aa;
        |    typeid ( aa ).name ();
        |}""".stripMargin());

    getOCSettings().SPACE_WITHIN_EMPTY_FUNCTION_CALL_PARENTHESES = true;
    assertFormat(
      """class A{
        |};
        |void bar() noexcept ( true );
        |void car() throw ( );
        |void foo() throw ( A ){
        |    alignas ( 16 ) int a[4];
        |    static_assert ( alignof ( a )==16 );
        |    static_assert ( sizeof ( a )%16==0 );
        |    auto f(int t)->decltype ( t ){
        |        return t;
        |    }
        |    auto g(int t)->typeof ( t ){
        |        return t;
        |    }
        |    A aa;
        |    typeid ( aa ).name ( );
        |}""".stripMargin());
  }

  public void testCPPLambdas() throws Exception {
    prepareForFormatCPP(
      """template<class T>
        |struct FooT {
        |    char g();
        |    auto f(T t)->decltype(t + g()){return t + g();}
        |};
        |int method(){
        |    int Y[] = {};
        |    int X[] = {1, 3, 5, 6, 7, 87, 1213, 2};
        |    int W[][3] = {{1, 3, 5}, {6, 7, 8}};
        |    [&](){};
        |    [=](){};
        |    [](){};
        |    auto la = [X, W](int i1, int i2)->bool mutable{return i1 < i2;}(1, 2);
        |}""".stripMargin());

    assertFormat(
      """template<class T> struct FooT{
        |    char g();
        |    auto f(T t)->decltype(t+g()){
        |        return t+g();
        |    }
        |};
        |int method(){
        |    int Y[]={};
        |    int X[]={1,3,5,6,7,87,1213,2};
        |    int W[][3]={{1,3,5},{6,7,8}};
        |    [&](){
        |    };
        |    [=](){
        |    };
        |    [](){
        |    };
        |    auto la=[X,W](int i1,int i2)->bool mutable{
        |        return i1<i2;
        |    }(1,2);
        |}""".stripMargin());

    getCommonSettings().SPACE_BEFORE_METHOD_PARENTHESES = true;
    assertFormat(
      """template<class T> struct FooT{
        |    char g ();
        |    auto f (T t)->decltype(t+g()){
        |        return t+g();
        |    }
        |};
        |int method (){
        |    int Y[]={};
        |    int X[]={1,3,5,6,7,87,1213,2};
        |    int W[][3]={{1,3,5},{6,7,8}};
        |    [&] (){
        |    };
        |    [=] (){
        |    };
        |    [] (){
        |    };
        |    auto la=[X,W] (int i1,int i2)->bool mutable{
        |        return i1<i2;
        |    }(1,2);
        |}""".stripMargin());

    getCommonSettings().SPACE_BEFORE_METHOD_CALL_PARENTHESES = true;
    assertFormat(
      """template<class T> struct FooT{
        |    char g ();
        |    auto f (T t)->decltype (t+g ()){
        |        return t+g ();
        |    }
        |};
        |int method (){
        |    int Y[]={};
        |    int X[]={1,3,5,6,7,87,1213,2};
        |    int W[][3]={{1,3,5},{6,7,8}};
        |    [&] (){
        |    };
        |    [=] (){
        |    };
        |    [] (){
        |    };
        |    auto la=[X,W] (int i1,int i2)->bool mutable{
        |        return i1<i2;
        |    } (1,2);
        |}""".stripMargin());

    getCommonSettings().SPACE_AROUND_LAMBDA_ARROW = true;
    assertFormat(
      """template<class T> struct FooT{
        |    char g ();
        |    auto f (T t) -> decltype (t+g ()){
        |        return t+g ();
        |    }
        |};
        |int method (){
        |    int Y[]={};
        |    int X[]={1,3,5,6,7,87,1213,2};
        |    int W[][3]={{1,3,5},{6,7,8}};
        |    [&] (){
        |    };
        |    [=] (){
        |    };
        |    [] (){
        |    };
        |    auto la=[X,W] (int i1,int i2) -> bool mutable{
        |        return i1<i2;
        |    } (1,2);
        |}""".stripMargin());

    getOCSettings().SPACE_WITHIN_FUNCTION_DECLARATION_PARENTHESES = true;
    assertFormat(
      """template<class T> struct FooT{
        |    char g ();
        |    auto f ( T t ) -> decltype (t+g ()){
        |        return t+g ();
        |    }
        |};
        |int method (){
        |    int Y[]={};
        |    int X[]={1,3,5,6,7,87,1213,2};
        |    int W[][3]={{1,3,5},{6,7,8}};
        |    [&] (){
        |    };
        |    [=] (){
        |    };
        |    [] (){
        |    };
        |    auto la=[X,W] ( int i1,int i2 ) -> bool mutable{
        |        return i1<i2;
        |    } (1,2);
        |}""".stripMargin());

    getOCSettings().SPACE_WITHIN_FUNCTION_CALL_PARENTHESES = true;
    assertFormat(
      """template<class T> struct FooT{
        |    char g ();
        |    auto f ( T t ) -> decltype ( t+g ()){
        |        return t+g ();
        |    }
        |};
        |int method (){
        |    int Y[]={};
        |    int X[]={1,3,5,6,7,87,1213,2};
        |    int W[][3]={{1,3,5},{6,7,8}};
        |    [&] (){
        |    };
        |    [=] (){
        |    };
        |    [] (){
        |    };
        |    auto la=[X,W] ( int i1,int i2 ) -> bool mutable{
        |        return i1<i2;
        |    } ( 1,2 );
        |}""".stripMargin());

    getCommonSettings().SPACE_BEFORE_METHOD_LBRACE = true;
    assertFormat(
      """template<class T> struct FooT{
        |    char g ();
        |    auto f ( T t ) -> decltype ( t+g ()) {
        |        return t+g ();
        |    }
        |};
        |int method () {
        |    int Y[]={};
        |    int X[]={1,3,5,6,7,87,1213,2};
        |    int W[][3]={{1,3,5},{6,7,8}};
        |    [&] () {
        |    };
        |    [=] () {
        |    };
        |    [] () {
        |    };
        |    auto la=[X,W] ( int i1,int i2 ) -> bool mutable {
        |        return i1<i2;
        |    } ( 1,2 );
        |}""".stripMargin());

    getCommonSettings().SPACE_WITHIN_ARRAY_INITIALIZER_BRACES = true;
    assertFormat(
      """template<class T> struct FooT{
        |    char g ();
        |    auto f ( T t ) -> decltype ( t+g ()) {
        |        return t+g ();
        |    }
        |};
        |int method () {
        |    int Y[]={};
        |    int X[]={ 1,3,5,6,7,87,1213,2 };
        |    int W[][3]={{ 1,3,5 },{ 6,7,8 }};
        |    [ & ] () {
        |    };
        |    [ = ] () {
        |    };
        |    [] () {
        |    };
        |    auto la=[ X,W ] ( int i1,int i2 ) -> bool mutable {
        |        return i1<i2;
        |    } ( 1,2 );
        |}""".stripMargin());

    getCommonSettings().SPACE_WITHIN_EMPTY_ARRAY_INITIALIZER_BRACES = true;
    assertFormat(
      """template<class T> struct FooT{
        |    char g ();
        |    auto f ( T t ) -> decltype ( t+g ()) {
        |        return t+g ();
        |    }
        |};
        |int method () {
        |    int Y[]={ };
        |    int X[]={ 1,3,5,6,7,87,1213,2 };
        |    int W[][3]={{ 1,3,5 },{ 6,7,8 }};
        |    [ & ] () {
        |    };
        |    [ = ] () {
        |    };
        |    [ ] () {
        |    };
        |    auto la=[ X,W ] ( int i1,int i2 ) -> bool mutable {
        |        return i1<i2;
        |    } ( 1,2 );
        |}""".stripMargin());
  }

  public void testCPPOperators() throws Exception {
    prepareForFormatCPP(
      """struct Foo {
        |  void foo(){}
        |  operator int(){}
        |  int operator   []  (int i);
        |  bool operator  !  () const {};
        |  int operator  ()  () {};
        |  bool operator  ==  (const  int  &  rhs)  {  };
        |  Foo operator  ++  (int)  volatile  {  };
        |  bool operator  <  (int i)  {  }
        |  int operator  <<  (int i)  {  }
        |  int operator  ,  (const int &i)  { return i + 1; }
        |};
        |int Foo :: operator  []  (  int i  )   {return 2;} """.stripMargin());

    assertFormat(
      """struct Foo{
        |    void foo(){
        |    }
        |    operator int(){
        |    }
        |    int operator[](int i);
        |    bool operator!() const{
        |    };
        |    int operator()(){
        |    };
        |    bool operator==(const int&rhs){
        |    };
        |    Foo operator++(int) volatile{
        |    };
        |    bool operator<(int i){
        |    }
        |    int operator<<(int i){
        |    }
        |    int operator,(const int&i){
        |        return i+1;
        |    }
        |};
        |int Foo::operator[](int i){
        |    return 2;
        |} """.stripMargin());

    getOCSettings().SPACE_BETWEEN_OPERATOR_AND_PUNCTUATOR = true;
    assertFormat(
      """struct Foo{
        |    void foo(){
        |    }
        |    operator int(){
        |    }
        |    int operator [](int i);
        |    bool operator !() const{
        |    };
        |    int operator ()(){
        |    };
        |    bool operator ==(const int&rhs){
        |    };
        |    Foo operator ++(int) volatile{
        |    };
        |    bool operator <(int i){
        |    }
        |    int operator <<(int i){
        |    }
        |    int operator ,(const int&i){
        |        return i+1;
        |    }
        |};
        |int Foo::operator [](int i){
        |    return 2;
        |} """.stripMargin());

    getCommonSettings().SPACE_BEFORE_METHOD_PARENTHESES = true;
    assertFormat(
      """struct Foo{
        |    void foo (){
        |    }
        |    operator int (){
        |    }
        |    int operator [] (int i);
        |    bool operator ! () const{
        |    };
        |    int operator () (){
        |    };
        |    bool operator == (const int&rhs){
        |    };
        |    Foo operator ++ (int) volatile{
        |    };
        |    bool operator < (int i){
        |    }
        |    int operator << (int i){
        |    }
        |    int operator , (const int&i){
        |        return i+1;
        |    }
        |};
        |int Foo::operator [] (int i){
        |    return 2;
        |} """.stripMargin());
  }

  public void testRemovingSpacesBeforeSemicolon() throws Exception {
    prepareForFormat("void foo()\n" +
                     ";\n" +
                     "void foo(){\n" +
                     "    foo()   ;\n" +
                     "    int i\n" +
                     "    ;\n" +
                     "    int j  ;\n" +
                     "}\n");

    getCommonSettings().KEEP_LINE_BREAKS = true;
    assertFormat("void foo();\n" +
                 "void foo(){\n" +
                 "    foo();\n" +
                 "    int i;\n" +
                 "    int j;\n" +
                 "}\n");

    getCommonSettings().KEEP_LINE_BREAKS = false;
    assertFormat("void foo();\n" +
                 "void foo(){\n" +
                 "    foo();\n" +
                 "    int i;\n" +
                 "    int j;\n" +
                 "}\n");
  }

  public void testSemicolonsInIfElse() throws Exception {
    prepareForFormat("void bar(){\n" +
                     "    if(1)\n" +
                     "    ;\n" +
                     "    else\n" +
                     "    ;\n" +
                     "}\n");

    getCommonSettings().KEEP_LINE_BREAKS = true;
    assertFormat("void bar(){\n" +
                 "    if(1);\n" +
                 "    else;\n" +
                 "}\n");

    getCommonSettings().KEEP_LINE_BREAKS = false;
    assertFormat("void bar(){\n" +
                 "    if(1);\n" +
                 "    else;\n" +
                 "}\n");
  }

  public void testKeepingSpaceBetweenIBOutletAndVariable() throws Exception {
    assertFormat("""#define IBOutlet __attribute__((IBOutlet))
                   |#define IBAction __attribute__((IBAction)) void
                   |@interface Foo{
                   |    IBOutlet NSObject*obj;
                   |}
                   |-(IBAction)method;
                   |@end""".stripMargin(),

                 """#define IBOutlet __attribute__((IBOutlet))
                   |#define IBAction __attribute__((IBAction)) void
                   |@interface Foo{
                   |    IBOutlet NSObject*obj;
                   |}
                   |-(IBAction)method;
                   |@end""".stripMargin());
  }

  public void testBeforeFunctionDeclarationParens() throws Exception {
    prepareForFormat("typedef void(fn)(char*);\n" +
                     "void foo(){\n" +
                     "}\n" +
                     "void foo();\n");

    getCommonSettings().SPACE_BEFORE_METHOD_PARENTHESES = false;
    assertFormat("typedef void(fn)(char*);\n" +
                 "void foo(){\n" +
                 "}\n" +
                 "void foo();\n");

    getCommonSettings().SPACE_BEFORE_METHOD_PARENTHESES = true;
    assertFormat("typedef void(fn) (char*);\n" +
                 "void foo (){\n" +
                 "}\n" +
                 "void foo ();\n");
  }

  public void testBeforeBlockDeclarationParens() throws Exception {
    prepareForFormat("typedef void(^block)(char*);\n" +
                     "void foo(){\n" +
                     "    ^(int i){\n" +
                     "    };\n" +
                     "    ^void(int i){\n" +
                     "    };\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_METHOD_PARENTHESES = false;
    assertFormat("typedef void(^block)(char*);\n" +
                 "void foo(){\n" +
                 "    ^(int i){\n" +
                 "    };\n" +
                 "    ^void(int i){\n" +
                 "    };\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_METHOD_PARENTHESES = true;
    assertFormat("typedef void(^block) (char*);\n" +
                 "void foo (){\n" +
                 "    ^(int i){\n" +
                 "    };\n" +
                 "    ^void (int i){\n" +
                 "    };\n" +
                 "}\n");
  }

  public void testBeforeFunctionCallParens() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    foo();\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_METHOD_CALL_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    foo();\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_METHOD_CALL_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    foo ();\n" +
                 "}\n");
  }

  public void testBeforeIfParens() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    if(true);\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_IF_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    if(true);\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_IF_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    if (true);\n" +
                 "}\n");
  }

  public void testBeforeForParens() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    for(int i=1;true;i++);\n" +
                     "    for(NSObject*o in nil);\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_FOR_PARENTHESES = false;

    assertFormat("void foo(){\n" +
                 "    for(int i=1;true;i++);\n" +
                 "    for(NSObject*o in nil);\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_FOR_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    for (int i=1;true;i++);\n" +
                 "    for (NSObject*o in nil);\n" +
                 "}\n");
  }

  public void testBeforeWhileParens() throws Exception {
    getCommonSettings().KEEP_CONTROL_STATEMENT_IN_ONE_LINE = true;
    prepareForFormat("void foo(){\n" +
                     "    while(true);\n" +
                     "    do;while(true);\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_WHILE_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    while(true);\n" +
                 "    do;while(true);\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_WHILE_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    while (true);\n" +
                 "    do;while (true);\n" +
                 "}\n");
  }

  public void testBeforeSwitchParens() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    switch(1){\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_SWITCH_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    switch(1){\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_SWITCH_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    switch (1){\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeCatchParensObjC() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    @try{\n" +
                     "    }@catch(NSException*exception){\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_CATCH_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    }@catch(NSException*exception){\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_CATCH_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    }@catch (NSException*exception){\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeCatchParensCPP() throws Exception {
    prepareForFormatCPP("void foo(){\n" +
                        "    try{\n" +
                        "    }catch(Exception*exception){\n" +
                        "    }\n" +
                        "}\n");

    getCommonSettings().SPACE_BEFORE_CATCH_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                    "    try{\n" +
                    "    }catch(Exception*exception){\n" +
                    "    }\n" +
                    "}\n");

    getCommonSettings().SPACE_BEFORE_CATCH_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                    "    try{\n" +
                    "    }catch (Exception*exception){\n" +
                    "    }\n" +
                    "}\n");
  }
  

  public void testBeforeSynchronizedParens() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    @synchronized(self){\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_SYNCHRONIZED_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    @synchronized(self){\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_SYNCHRONIZED_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    @synchronized (self){\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeAndAfterPropertyAttributesParens() throws Exception {
    prepareForFormat("@interface Foo\n" +
                     "@property(readonly,retain)int p;\n" +
                     "@end\n");

    getOCSettings().SPACE_BEFORE_PROPERTY_ATTRIBUTES_PARENTHESES = false;
    assertFormat("@interface Foo\n" +
                 "@property(readonly,retain) int p;\n" +
                 "@end\n");

    getOCSettings().SPACE_BEFORE_PROPERTY_ATTRIBUTES_PARENTHESES = true;
    assertFormat("@interface Foo\n" +
                 "@property (readonly,retain) int p;\n" +
                 "@end\n");
  }

  public void testAroundAssignmentOperators() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=1;\n" +
                     "    i=2;\n" +
                     "    i+=2;\n" +
                     "}\n");

    getCommonSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS = false;
    assertFormat("void foo(){\n" +
                 "    int i=1;\n" +
                 "    i=2;\n" +
                 "    i+=2;\n" +
                 "}\n");

    getCommonSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS = true;
    assertFormat("void foo(){\n" +
                 "    int i = 1;\n" +
                 "    i = 2;\n" +
                 "    i += 2;\n" +
                 "}\n");
  }

  public void testAroundInSynthesizes() throws Exception {
    prepareForFormat("@implementation Foo\n" +
                     "@synthesize x=y,z=w;\n" +
                     "@end\n");

    getCommonSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS = false;
    assertFormat("@implementation Foo\n" +
                 "@synthesize x=y,z=w;\n" +
                 "@end\n");

    getCommonSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS = true;
    assertFormat("@implementation Foo\n" +
                 "@synthesize x = y,z = w;\n" +
                 "@end\n");
  }

  public void testAroundLogicalOperators() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=1&&0||1;\n" +
                     "}\n");

    getCommonSettings().SPACE_AROUND_LOGICAL_OPERATORS = false;
    assertFormat("void foo(){\n" +
                 "    int i=1&&0||1;\n" +
                 "}\n");

    getCommonSettings().SPACE_AROUND_LOGICAL_OPERATORS = true;
    assertFormat("void foo(){\n" +
                 "    int i=1 && 0 || 1;\n" +
                 "}\n");
  }

  public void testAroundEqualityOperators() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=1==0!=1;\n" +
                     "}\n");

    getCommonSettings().SPACE_AROUND_EQUALITY_OPERATORS = false;
    assertFormat("void foo(){\n" +
                 "    int i=1==0!=1;\n" +
                 "}\n");

    getCommonSettings().SPACE_AROUND_EQUALITY_OPERATORS = true;
    assertFormat("void foo(){\n" +
                 "    int i=1 == 0 != 1;\n" +
                 "}\n");
  }

  public void testAroundRelationalOperators() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=1>2<3>=4<=5;\n" +
                     "}\n");

    getCommonSettings().SPACE_AROUND_RELATIONAL_OPERATORS = false;
    assertFormat("void foo(){\n" +
                 "    int i=1>2<3>=4<=5;\n" +
                 "}\n");

    getCommonSettings().SPACE_AROUND_RELATIONAL_OPERATORS = true;
    assertFormat("void foo(){\n" +
                 "    int i=1 > 2 < 3 >= 4 <= 5;\n" +
                 "}\n");
  }

  public void testAroundBitwiseOperators() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=1|2&3^4;\n" +
                     "}\n");

    getCommonSettings().SPACE_AROUND_BITWISE_OPERATORS = false;
    assertFormat("void foo(){\n" +
                 "    int i=1|2&3^4;\n" +
                 "}\n");

    getCommonSettings().SPACE_AROUND_BITWISE_OPERATORS = true;
    assertFormat("void foo(){\n" +
                 "    int i=1 | 2 & 3 ^ 4;\n" +
                 "}\n");
  }

  public void testAroundAdditiveOperators() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=1+2-3;\n" +
                     "}\n");

    getCommonSettings().SPACE_AROUND_ADDITIVE_OPERATORS = false;
    assertFormat("void foo(){\n" +
                 "    int i=1+2-3;\n" +
                 "}\n");

    getCommonSettings().SPACE_AROUND_ADDITIVE_OPERATORS = true;
    assertFormat("void foo(){\n" +
                 "    int i=1 + 2 - 3;\n" +
                 "}\n");
  }

  public void testAroundMultiplicativeOperators() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=1*2/3%4;\n" +
                     "}\n");

    getCommonSettings().SPACE_AROUND_MULTIPLICATIVE_OPERATORS = false;
    assertFormat("void foo(){\n" +
                 "    int i=1*2/3%4;\n" +
                 "}\n");

    getCommonSettings().SPACE_AROUND_MULTIPLICATIVE_OPERATORS = true;
    assertFormat("void foo(){\n" +
                 "    int i=1 * 2 / 3 % 4;\n" +
                 "}\n");
  }

  public void testAroundShiftOperators() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=1<<2>>3;\n" +
                     "}\n");

    getCommonSettings().SPACE_AROUND_SHIFT_OPERATORS = false;
    assertFormat("void foo(){\n" +
                 "    int i=1<<2>>3;\n" +
                 "}\n");

    getCommonSettings().SPACE_AROUND_SHIFT_OPERATORS = true;
    assertFormat("void foo(){\n" +
                 "    int i=1 << 2 >> 3;\n" +
                 "}\n");
  }

  public void testAroundUnaryOperators() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=!true;\n" +
                     "    int i=-1;\n" +
                     "    int j=~1;\n" +
                     "    i++;\n" +
                     "    ++i;\n" +
                     "    int x=*NULL;\n" +
                     "    void*p=&x;\n" +
                     "    void*p=*x;\n" +
                     "    void*p=**x;\n" +
                     "    void*pp=(void*)p;\n" +
                     "}\n");

    getCommonSettings().SPACE_AROUND_UNARY_OPERATOR = false;
    assertFormat("void foo(){\n" +
                 "    int i=!true;\n" +
                 "    int i=-1;\n" +
                 "    int j=~1;\n" +
                 "    i++;\n" +
                 "    ++i;\n" +
                 "    int x=*NULL;\n" +
                 "    void*p=&x;\n" +
                 "    void*p=*x;\n" +
                 "    void*p=**x;\n" +
                 "    void*pp=(void*)p;\n" +
                 "}\n");

    getCommonSettings().SPACE_AROUND_UNARY_OPERATOR = true;
    assertFormat("void foo(){\n" +
                 "    int i=! true;\n" +
                 "    int i=- 1;\n" +
                 "    int j=~ 1;\n" +
                 "    i ++;\n" +
                 "    ++ i;\n" +
                 // * and & are covered by other formatting options
                 "    int x=*NULL;\n" +
                 "    void*p=&x;\n" +
                 "    void*p=*x;\n" +
                 "    void*p=**x;\n" +
                 "    void*pp=(void*)p;\n" +
                 "}\n");
  }

  public void testAroundOperatorsRedCode() throws Exception {
    assertFormatCPP("---i---", "---i---");
    assertFormatCPP("----i----",  "----i----");
    assertFormatCPP("int i = ---i---", "int i=-- -i-- -");
    assertFormatCPP("int i = ----i----",  "int i=-- --i-- --");
  }

  public void testAroundOperatorsInCPP() throws Exception {
    prepareForFormatCPP("int  k  =  +  ++  9  --  ;\n" +
                        "void foo(){  -  -  k;}\n" +
                        "int  o  =  k  &  &  k;\n" +
                        "int  i  =  -  --  k  -  +  5;\n" +
                        "int  m  =  k  &&  &  k  &&  ^{ return 2; };\n" +
                        "int  n  =  -  -  !  !  k;")
    assertFormat("int k=+ ++9--;\n" +
                 "void foo(){\n" +
                 "    - -k;\n" +
                 "}\n" +
                 "int o=k& &k;\n" +
                 "int i=- --k- +5;\n" +
                 "int m=k&& &k&&^{\n" +
                 "    return 2;\n" +
                 "};\n" +
                 "int n=- -!!k;");

    getCommonSettings().SPACE_AROUND_UNARY_OPERATOR = true;
    getCommonSettings().SPACE_AROUND_EQUALITY_OPERATORS = true;
    getCommonSettings().SPACE_AROUND_LOGICAL_OPERATORS = true;
    getCommonSettings().SPACE_AROUND_BITWISE_OPERATORS = true;
    getCommonSettings().SPACE_AROUND_ADDITIVE_OPERATORS = true;
    getCommonSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS = true;

    assertFormat("int k = + ++ 9 --;\n" +
                 "void foo(){\n" +
                 "    - - k;\n" +
                 "}\n" +
                 "int o = k & &k;\n" +
                 "int i = - -- k - + 5;\n" +
                 "int m = k && &k && ^{\n" +
                 "    return 2;\n" +
                 "};\n" +
                 "int n = - - ! ! k;");
  }

  public void testAroundTextualOperatorsInCPP() throws Exception {
    prepareForFormatCPP("void foo(){\n" +
                        "    int i=not true and false or true;\n" +
                        "    i and_eq 1 or_eq 2 not_eq 3;\n" +
                        "}\n");

    getCommonSettings().SPACE_AROUND_UNARY_OPERATOR = false;
    getCommonSettings().SPACE_AROUND_EQUALITY_OPERATORS = false;
    getCommonSettings().SPACE_AROUND_LOGICAL_OPERATORS = false;
    getCommonSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS = false;

    assertFormat("void foo(){\n" +
                 "    int i=not true and false or true;\n" +
                 "    i and_eq 1 or_eq 2 not_eq 3;\n" +
                 "}\n");

    getCommonSettings().SPACE_AROUND_UNARY_OPERATOR = true;
    getCommonSettings().SPACE_AROUND_EQUALITY_OPERATORS = true;
    getCommonSettings().SPACE_AROUND_LOGICAL_OPERATORS = true;
    getCommonSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS = true;

    assertFormat("void foo(){\n" +
                 "    int i = not true and false or true;\n" +
                 "    i and_eq 1 or_eq 2 not_eq 3;\n" +
                 "}\n");
  }

  public void testSpacesAroundDereferencingOperators() throws Exception {
    prepareForFormatCPP(
      "namespace NS{\n" +
      "class Foo{\n" +
      "public :\n" +
      "    void method(){\n" +
      "      this->method();\n" +
      "      (*this).method();\n" +
      "    }\n" +
      "};\n" +
      "}\n" +
      "typedef void (NS::Foo::*FOO_call)();\n" +
      "void foo(){\n" +
      "    NS::Foo *foo=new NS::Foo();\n" +
      "    NS::Foo &pfoo=NS::Foo();\n" +
      "    FOO_call pfnTempCall=(FOO_call)(&NS::Foo::method);\n" +
      "    (foo ->* pfnTempCall)();\n" +
      "    (pfoo .* pfnTempCall)();\n" +
      "    ((foo) ->* pfnTempCall)();\n" +
      "    ((pfoo) .* pfnTempCall)();\n" +
      "}\n");

    getOCSettings().SPACE_AROUND_PM_OPERATORS = false;
    assertFormat(
      "namespace NS{\n" +
      "class Foo{\n" +
      "public :\n" +
      "    void method(){\n" +
      "        this->method();\n" +
      "        (*this).method();\n" +
      "    }\n" +
      "};\n" +
      "}\n" +
      "typedef void (NS::Foo::*FOO_call)();\n" +
      "void foo(){\n" +
      "    NS::Foo*foo=new NS::Foo();\n" +
      "    NS::Foo&pfoo=NS::Foo();\n" +
      "    FOO_call pfnTempCall=(FOO_call)(&NS::Foo::method);\n" +
      "    (foo->*pfnTempCall)();\n" +
      "    (pfoo.*pfnTempCall)();\n" +
      "    ((foo)->*pfnTempCall)();\n" +
      "    ((pfoo).*pfnTempCall)();\n" +
      "}\n");

    getOCSettings().SPACE_AROUND_PM_OPERATORS = true;
    assertFormat(
      "namespace NS{\n" +
      "class Foo{\n" +
      "public :\n" +
      "    void method(){\n" +
      "        this -> method();\n" +
      "        (*this) . method();\n" +
      "    }\n" +
      "};\n" +
      "}\n" +
      "typedef void (NS::Foo::*FOO_call)();\n" +
      "void foo(){\n" +
      "    NS::Foo*foo=new NS::Foo();\n" +
      "    NS::Foo&pfoo=NS::Foo();\n" +
      "    FOO_call pfnTempCall=(FOO_call)(&NS::Foo::method);\n" +
      "    (foo ->* pfnTempCall)();\n" +
      "    (pfoo .* pfnTempCall)();\n" +
      "    ((foo) ->* pfnTempCall)();\n" +
      "    ((pfoo) .* pfnTempCall)();\n" +
      "}\n");

    prepareForFormatCPP("struct A{\n" +
                        "};\n" +
                        "A operator ->*(A l,A r){\n" +
                        "}\n" +
                        "int main(){\n" +
                        "    A a;\n" +
                        "    a->*a;\n" +
                        "    (a)->*a;\n" +
                        "}\n");

    getOCSettings().SPACE_AROUND_PM_OPERATORS = false;
    assertFormat("struct A{\n" +
                        "};\n" +
                        "A operator->*(A l,A r){\n" +
                        "}\n" +
                        "int main(){\n" +
                        "    A a;\n" +
                        "    a->*a;\n" +
                        "    (a)->*a;\n" +
                        "}\n");

    getOCSettings().SPACE_AROUND_PM_OPERATORS = true;
    assertFormat("struct A{\n" +
                        "};\n" +
                        "A operator->*(A l,A r){\n" +
                        "}\n" +
                        "int main(){\n" +
                        "    A a;\n" +
                        "    a ->* a;\n" +
                        "    (a) ->* a;\n" +
                        "}\n");
  }

  public void testBeforeAfterPointerInDeclaration() throws Exception {
    prepareForFormat("@interface Foo\n" +
                     "-(void**)foo:(void* *)p;\n" +
                     "@end\n" +
                     "void**foo(void**p,void* *,void*(),void(*)(),void*(*)()){\n" +
                     "    void*p,*e,* *ee;\n" +
                     "    void**pp=(void**)p;\n" +
                     "}\n" +
                     "int*const*const foo(int*const x);");

    getOCSettings().SPACE_BEFORE_POINTER_IN_DECLARATION = false;
    assertFormat("@interface Foo\n" +
                 "-(void**)foo:(void**)p;\n" +
                 "@end\n" +
                 "void**foo(void**p,void**,void*(),void(*)(),void*(*)()){\n" +
                 "    void*p,*e,**ee;\n" +
                 "    void**pp=(void**)p;\n" +
                 "}\n" +
                 "int*const*const foo(int*const x);");

    getOCSettings().SPACE_BEFORE_POINTER_IN_DECLARATION = true;
    assertFormat("@interface Foo\n" +
                 "-(void **)foo:(void **)p;\n" +
                 "@end\n" +
                 "void **foo(void **p,void **,void *(),void(*)(),void *(*)()){\n" +
                 "    void *p,*e,**ee;\n" +
                 "    void **pp=(void **)p;\n" +
                 "}\n" +
                 "int *const *const foo(int *const x);");

    getOCSettings().SPACE_BEFORE_POINTER_IN_DECLARATION = false;
    getOCSettings().SPACE_AFTER_POINTER_IN_DECLARATION = false;
    assertFormat("@interface Foo\n" +
                 "-(void**)foo:(void**)p;\n" +
                 "@end\n" +
                 "void**foo(void**p,void**,void*(),void(*)(),void*(*)()){\n" +
                 "    void*p,*e,**ee;\n" +
                 "    void**pp=(void**)p;\n" +
                 "}\n" +
                 "int*const*const foo(int*const x);");

    getOCSettings().SPACE_AFTER_POINTER_IN_DECLARATION = true;
    assertFormat("@interface Foo\n" +
                 "-(void**)foo:(void**)p;\n" +
                 "@end\n" +
                 "void** foo(void** p,void**,void*(),void(*)(),void* (*)()){\n" +
                 "    void* p,* e,** ee;\n" +
                 "    void** pp=(void**)p;\n" +
                 "}\n" +
                 "int* const* const foo(int* const x);");
  }

  public void testBeforeAfterReferenceInDeclaration() throws Exception {
    prepareForFormatCPP("typedef int X;\n" +
                        "int&foo1(int&&x) {\n" +
                        "  int * * &p=(int * * &)x;\n" +
                        "  * * + p; -- * p;\n" +
                        "  int static&r=*&x;\n" +
                        "  X&r1=r,&r0=*&*&x;\n" +
                        "  static X&r2=*&r;\n" +
                        "  return r1&&(r2&x)?r:x;\n" +
                        "}\n" +
                        "int&foo2(X&&const x) {\n" +
                        "  return const_cast<int&>(x);\n" +
                        "}\n" +
                        "X const &foo3(int const&const x) {\n" +
                        "  return const_cast<X const &>(x);\n" +
                        "}\n" +
                        "X foo4(int&&const) {\n" +
                        "  return (X&)\"&\";\n" +
                        "}");

    getOCSettings().SPACE_BEFORE_REFERENCE_IN_DECLARATION = false;
    getOCSettings().SPACE_AFTER_REFERENCE_IN_DECLARATION = false;
    getCommonSettings().SPACE_AROUND_BITWISE_OPERATORS = true;
    getCommonSettings().SPACE_AROUND_LOGICAL_OPERATORS = true;
    getCommonSettings().SPACE_AROUND_UNARY_OPERATOR = true;
    assertFormat("typedef int X;\n" +
                 "int&foo1(int&&x){\n" +
                 "    int**&p=(int**&)x;\n" +
                 "    **+ p;\n" +
                 "    -- *p;\n" +
                 "    int static&r=*&x;\n" +
                 "    X&r1=r,&r0=*&*&x;\n" +
                 "    static X&r2=*&r;\n" +
                 "    return r1 && (r2 & x)?r:x;\n" +
                 "}\n" +
                 "int&foo2(X&&const x){\n" +
                 "    return const_cast<int&>(x);\n" +
                 "}\n" +
                 "X const&foo3(int const&const x){\n" +
                 "    return const_cast<X const&>(x);\n" +
                 "}\n" +
                 "X foo4(int&&const){\n" +
                 "    return (X&)\"&\";\n" +
                 "}");

    getOCSettings().SPACE_BEFORE_REFERENCE_IN_DECLARATION = true;
    getOCSettings().SPACE_AFTER_REFERENCE_IN_DECLARATION = true;
    getOCSettings().SPACE_AFTER_REFERENCE_IN_RVALUE = false;
    getCommonSettings().SPACE_AROUND_BITWISE_OPERATORS = false;
    getCommonSettings().SPACE_AROUND_LOGICAL_OPERATORS = false;
    getCommonSettings().SPACE_AROUND_UNARY_OPERATOR = false;
    assertFormat("typedef int X;\n" +
                 "int & foo1(int && x){\n" +
                 "    int**& p=(int**&)x;\n" +
                 "    **+p;\n" +
                 "    --*p;\n" +
                 "    int static & r=*&x;\n" +
                 "    X & r1=r,& r0=*&*&x;\n" +
                 "    static X & r2=*&r;\n" +
                 "    return r1&&(r2&x)?r:x;\n" +
                 "}\n" +
                 "int & foo2(X && const x){\n" +
                 "    return const_cast<int &>(x);\n" +
                 "}\n" +
                 "X const & foo3(int const & const x){\n" +
                 "    return const_cast<X const &>(x);\n" +
                 "}\n" +
                 "X foo4(int && const){\n" +
                 "    return (X &)\"&\";\n" +
                 "}");

    getOCSettings().SPACE_AFTER_REFERENCE_IN_DECLARATION = false;
    getOCSettings().SPACE_AFTER_REFERENCE_IN_RVALUE = true;
    assertFormat("typedef int X;\n" +
                 "int &foo1(int &&x){\n" +
                 "    int**&p=(int**&)x;\n" +
                 "    ** +p;\n" +
                 "    --* p;\n" +
                 "    int static &r=*& x;\n" +
                 "    X &r1=r,&r0=*&*& x;\n" +
                 "    static X &r2=*& r;\n" +
                 "    return r1&&(r2&x)?r:x;\n" +
                 "}\n" +
                 "int &foo2(X &&const x){\n" +
                 "    return const_cast<int &>(x);\n" +
                 "}\n" +
                 "X const &foo3(int const &const x){\n" +
                 "    return const_cast<X const &>(x);\n" +
                 "}\n" +
                 "X foo4(int &&const){\n" +
                 "    return (X &)\"&\";\n" +
                 "}");

    getOCSettings().SPACE_AFTER_REFERENCE_IN_DECLARATION = true;
    getOCSettings().SPACE_BEFORE_REFERENCE_IN_DECLARATION = false;
    getOCSettings().SPACE_AFTER_REFERENCE_IN_RVALUE = false;
    assertFormat("typedef int X;\n" +
                 "int& foo1(int&& x){\n" +
                 "    int**& p=(int**&)x;\n" +
                 "    **+p;\n" +
                 "    --*p;\n" +
                 "    int static& r=*&x;\n" +
                 "    X& r1=r,& r0=*&*&x;\n" +
                 "    static X& r2=*&r;\n" +
                 "    return r1&&(r2&x)?r:x;\n" +
                 "}\n" +
                 "int& foo2(X&& const x){\n" +
                 "    return const_cast<int&>(x);\n" +
                 "}\n" +
                 "X const& foo3(int const& const x){\n" +
                 "    return const_cast<X const&>(x);\n" +
                 "}\n" +
                 "X foo4(int&& const){\n" +
                 "    return (X&)\"&\";\n" +
                 "}");
  }

  public void testBeforeNamespaceLBrace() throws Exception {
    prepareForFormatCPP("namespace Foo{\n" +
                        "}\n");

    getOCSettings().SPACE_BEFORE_NAMESPACE_LBRACE = false;
    assertFormat("namespace Foo{\n" +
                 "}\n");

    getOCSettings().SPACE_BEFORE_NAMESPACE_LBRACE = true;
    assertFormat("namespace Foo {\n" +
                 "}\n");
  }

  public void testBeforeInterfaceOrStructLBrace() throws Exception {
    prepareForFormat("@interface Foo{\n" +
                     "}\n" +
                     "@end\n" +
                     "struct foo_t{\n" +
                     "};\n" +
                     "struct{\n" +
                     "};\n" +
                     "enum foo_e{\n" +
                     "};\n" +
                     "enum{\n" +
                     "};\n" +
                     "union foo_u{\n" +
                     "};\n" +
                     "union{\n" +
                     "};\n");

    getCommonSettings().SPACE_BEFORE_CLASS_LBRACE = false;
    assertFormat("@interface Foo{\n" +
                 "}\n" +
                 "@end\n" +
                 "struct foo_t{\n" +
                 "};\n" +
                 "struct{\n" +
                 "};\n" +
                 "enum foo_e{\n" +
                 "};\n" +
                 "enum{\n" +
                 "};\n" +
                 "union foo_u{\n" +
                 "};\n" +
                 "union{\n" +
                 "};\n");

    getCommonSettings().SPACE_BEFORE_CLASS_LBRACE = true;
    assertFormat("@interface Foo {\n" +
                 "}\n" +
                 "@end\n" +
                 "struct foo_t {\n" +
                 "};\n" +
                 "struct {\n" +
                 "};\n" +
                 "enum foo_e {\n" +
                 "};\n" +
                 "enum {\n" +
                 "};\n" +
                 "union foo_u {\n" +
                 "};\n" +
                 "union {\n" +
                 "};\n");
  }

  public void testAfterStructRBrace() throws Exception {
    prepareForFormat("struct{\n" +
                     "}foo_t;\n" +
                     "struct{\n" +
                     "};\n" +
                     "enum{\n" +
                     "}foo_e;\n" +
                     "enum{\n" +
                     "};\n" +
                     "union{\n" +
                     "}foo_u;\n" +
                     "union{\n" +
                     "};\n");

    getOCSettings().SPACE_AFTER_STRUCTURES_RBRACE = false;
    assertFormat("struct{\n" +
                 "}foo_t;\n" +
                 "struct{\n" +
                 "};\n" +
                 "enum{\n" +
                 "}foo_e;\n" +
                 "enum{\n" +
                 "};\n" +
                 "union{\n" +
                 "}foo_u;\n" +
                 "union{\n" +
                 "};\n");

    getOCSettings().SPACE_AFTER_STRUCTURES_RBRACE = true;
    assertFormat("struct{\n" +
                 "} foo_t;\n" +
                 "struct{\n" +
                 "};\n" +
                 "enum{\n" +
                 "} foo_e;\n" +
                 "enum{\n" +
                 "};\n" +
                 "union{\n" +
                 "} foo_u;\n" +
                 "union{\n" +
                 "};\n");
  }

  public void testAfterStructRBraceDoesntTakeEffectInCastsAndTypes() throws Exception {
    getOCSettings().SPACE_AFTER_STRUCTURES_RBRACE = true;
    assertFormat("struct s foo=(struct s)0\n" +
                 "struct s*foo=(struct s*)0\n",

                 "struct s foo=(struct s)0\n" +
                 "struct s*foo=(struct s*)0\n");
  }

  public void testBeforeMethodLBrace() throws Exception {
    prepareForFormat("@implementation Foo\n" +
                     "-(void)foo{\n" +
                     "}\n" +
                     "@end\n" +
                     "void foo(){\n" +
                     "    return ^{\n" +
                     "    };\n" +
                     "    return ^(int i){\n" +
                     "    };\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_METHOD_LBRACE = false;
    assertFormat("@implementation Foo\n" +
                 "-(void)foo{\n" +
                 "}\n" +
                 "@end\n" +
                 "void foo(){\n" +
                 "    return ^{\n" +
                 "    };\n" +
                 "    return ^(int i){\n" +
                 "    };\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_METHOD_LBRACE = true;
    assertFormat("@implementation Foo\n" +
                 "-(void)foo {\n" +
                 "}\n" +
                 "@end\n" +
                 "void foo() {\n" +
                 "    return ^{\n" +
                 "    };\n" +
                 "    return ^(int i) {\n" +
                 "    };\n" +
                 "}\n");
  }

  public void testBeforeIfLBrace() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    if(true){\n" +
                     "    }else if(false){\n" +
                     "    }else{\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_IF_LBRACE = false;
    assertFormat("void foo(){\n" +
                 "    if(true){\n" +
                 "    }else if(false){\n" +
                 "    }else{\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_IF_LBRACE = true;
    assertFormat("void foo(){\n" +
                 "    if(true) {\n" +
                 "    }else if(false) {\n" +
                 "    }else{\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeElseLBrace() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    if(true){\n" +
                     "    }else if(false){\n" +
                     "    }else{\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_ELSE_LBRACE = false;
    assertFormat("void foo(){\n" +
                 "    if(true){\n" +
                 "    }else if(false){\n" +
                 "    }else{\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_ELSE_LBRACE = true;
    assertFormat("void foo(){\n" +
                 "    if(true){\n" +
                 "    }else if(false){\n" +
                 "    }else {\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeElseKeyword() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    if(true){\n" +
                     "    }else if(false){\n" +
                     "    }else{\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_ELSE_KEYWORD = false;
    assertFormat("void foo(){\n" +
                 "    if(true){\n" +
                 "    }else if(false){\n" +
                 "    }else{\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_ELSE_KEYWORD = true;
    assertFormat("void foo(){\n" +
                 "    if(true){\n" +
                 "    } else if(false){\n" +
                 "    } else{\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeForLBrace() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    for(int i=0;true;i++){\n" +
                     "    }\n" +
                     "    for(NSObject*o in nil){\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_FOR_LBRACE = false;
    assertFormat("void foo(){\n" +
                 "    for(int i=0;true;i++){\n" +
                 "    }\n" +
                 "    for(NSObject*o in nil){\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_FOR_LBRACE = true;
    assertFormat("void foo(){\n" +
                 "    for(int i=0;true;i++) {\n" +
                 "    }\n" +
                 "    for(NSObject*o in nil) {\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeWhileLBrace() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    while(true){\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_WHILE_LBRACE = false;
    assertFormat("void foo(){\n" +
                 "    while(true){\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_WHILE_LBRACE = true;
    assertFormat("void foo(){\n" +
                 "    while(true) {\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeDoLBrace() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    do{\n" +
                     "    }while(true);\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_DO_LBRACE = false;
    assertFormat("void foo(){\n" +
                 "    do{\n" +
                 "    }while(true);\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_DO_LBRACE = true;
    assertFormat("void foo(){\n" +
                 "    do {\n" +
                 "    }while(true);\n" +
                 "}\n");
  }

  public void testBeforeWhileKeyword() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    do{\n" +
                     "    }while(true);\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_WHILE_KEYWORD = false;
    assertFormat("void foo(){\n" +
                 "    do{\n" +
                 "    }while(true);\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_WHILE_KEYWORD = true;
    assertFormat("void foo(){\n" +
                 "    do{\n" +
                 "    } while(true);\n" +
                 "}\n");
  }

  public void testBeforeSwitchLBrace() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    switch(1){\n" +
                     "    case 1:\n" +
                     "        return;\n" +
                     "    case 2:{\n" +
                     "        doSomething();\n" +
                     "    }\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_SWITCH_LBRACE = false;
    assertFormat("void foo(){\n" +
                 "    switch(1){\n" +
                 "    case 1:\n" +
                 "        return;\n" +
                 "    case 2:{\n" +
                 "        doSomething();\n" +
                 "    }\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_SWITCH_LBRACE = true;
    assertFormat("void foo(){\n" +
                 "    switch(1) {\n" +
                 "    case 1:\n" +
                 "        return;\n" +
                 "    case 2: {\n" +
                 "        doSomething();\n" +
                 "    }\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeTryLBrace() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    @try{\n" +
                     "    }@catch(NSException*exception){\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_TRY_LBRACE = false;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    }@catch(NSException*exception){\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_TRY_LBRACE = true;
    assertFormat("void foo(){\n" +
                 "    @try {\n" +
                 "    }@catch(NSException*exception){\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeCatchLBrace() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    @try{\n" +
                     "    }@catch(NSException*exception){\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_CATCH_LBRACE = false;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    }@catch(NSException*exception){\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_CATCH_LBRACE = true;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    }@catch(NSException*exception) {\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeCatchKeyword() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    @try{\n" +
                     "    }@catch(NSException*exception){\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_CATCH_KEYWORD = false;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    }@catch(NSException*exception){\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_CATCH_KEYWORD = true;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    } @catch(NSException*exception){\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeFinallyLBrace() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    @try{\n" +
                     "    }@finally{\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_FINALLY_LBRACE = false;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    }@finally{\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_FINALLY_LBRACE = true;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    }@finally {\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeFinallyKeyword() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    @try{\n" +
                     "    }@finally{\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_FINALLY_KEYWORD = false;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    }@finally{\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_FINALLY_KEYWORD = true;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    } @finally{\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeSynchronizedLBrace() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    @synchronized(self){\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_SYNCHRONIZED_LBRACE = false;
    assertFormat("void foo(){\n" +
                 "    @synchronized(self){\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_SYNCHRONIZED_LBRACE = true;
    assertFormat("void foo(){\n" +
                 "    @synchronized(self) {\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testBeforeAutoreleasePoolLBrace() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    @autoreleasepool{\n" +
                     "    }\n" +
                     "}\n");

    getOCSettings().SPACE_BEFORE_AUTORELEASE_POOL_LBRACE = false;
    assertFormat("void foo(){\n" +
                 "    @autoreleasepool{\n" +
                 "    }\n" +
                 "}\n");

    getOCSettings().SPACE_BEFORE_AUTORELEASE_POOL_LBRACE = true;
    assertFormat("void foo(){\n" +
                 "    @autoreleasepool {\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testWithinArrayBrackets() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i[];\n" +
                     "    int j[3];\n" +
                     "    i[1];\n" +
                     "    [NSObject alloc];\n" +
                     "}\n");

    getCommonSettings().SPACE_WITHIN_BRACKETS = false;
    assertFormat("void foo(){\n" +
                 "    int i[];\n" +
                 "    int j[3];\n" +
                 "    i[1];\n" +
                 "    [NSObject alloc];\n" +
                 "}\n");

    getCommonSettings().SPACE_WITHIN_BRACKETS = true;
    assertFormat("void foo(){\n" +
                 "    int i[];\n" +
                 "    int j[3];\n" +
                 "    i[ 1 ];\n" +
                 "    [NSObject alloc];\n" +
                 "}\n");
  }

  public void testWithinCollectionAndStructureInitializerBraces() throws Exception {
    prepareForFormat("struct st{\n" +
                     "    int x;\n" +
                     "    int y;\n" +
                     "    int z;\n" +
                     "};\n" +
                     "void foo(){\n" +
                     "    int i[]={1,2,3};\n" +
                     "    id array=@[@1,@2];\n" +
                     "    id dictionary=@{@\"one\":@1,@\"two\":@2};\n" +
                     "    st s={1,2};\n" +
                     "}\n");

    // these options should not affect formatting:
    getCommonSettings().SPACE_WITHIN_BRACKETS = true;

    getCommonSettings().SPACE_WITHIN_ARRAY_INITIALIZER_BRACES = false;
    assertFormat("struct st{\n" +
                 "    int x;\n" +
                 "    int y;\n" +
                 "    int z;\n" +
                 "};\n" +
                 "void foo(){\n" +
                 "    int i[]={1,2,3};\n" +
                 "    id array=@[@1,@2];\n" +
                 "    id dictionary=@{@\"one\":@1,@\"two\":@2};\n" +
                 "    st s={1,2};\n" +
                 "}\n");

    getCommonSettings().SPACE_WITHIN_ARRAY_INITIALIZER_BRACES = true;
    assertFormat("struct st{\n" +
                 "    int x;\n" +
                 "    int y;\n" +
                 "    int z;\n" +
                 "};\n" +
                 "void foo(){\n" +
                 "    int i[]={ 1,2,3 };\n" +
                 "    id array=@[ @1,@2 ];\n" +
                 "    id dictionary=@{ @\"one\":@1,@\"two\":@2 };\n" +
                 "    st s={ 1,2 };\n" +
                 "}\n");
  }

  public void testAroundDictionaryLiteralColon() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    id dictionary=@{@\"one\":@1,@\"two\":@2};\n" +
                     "}\n");

    getOCSettings().SPACE_AROUND_DICTIONARY_LITERAL_COLON = false;
    assertFormat("void foo(){\n" +
                 "    id dictionary=@{@\"one\":@1,@\"two\":@2};\n" +
                 "}\n");

    getOCSettings().SPACE_AROUND_DICTIONARY_LITERAL_COLON = true;
    assertFormat("void foo(){\n" +
                 "    id dictionary=@{@\"one\" : @1,@\"two\" : @2};\n" +
                 "}\n");
  }

  public void testAroundDictionaryLiteralColonDoesntAffectOtherColons() throws Exception {
    getOCSettings().SPACE_AROUND_DICTIONARY_LITERAL_COLON = true;
    assertFormatCPP("class Foo:Bar{\n" +
                    "};\n" +
                    "@interface Foo:NSObject\n" +
                    "-(void)foo:(int)i;\n" +
                    "@end\n" +
                    "@implementation Foo\n" +
                    "-(void)foo:(int)i{\n" +
                    "    [self foo:1];\n" +
                    "    int i=1?2:3;\n" +
                    "    for(int i:j);\n" +
                    "}\n" +
                    "@end\n",

                    "class Foo:Bar{\n" +
                    "};\n" +
                    "@interface Foo:NSObject\n" +
                    "-(void)foo:(int)i;\n" +
                    "@end\n" +
                    "@implementation Foo\n" +
                    "-(void)foo:(int)i{\n" +
                    "    [self foo:1];\n" +
                    "    int i=1?2:3;\n" +
                    "    for(int i:j);\n" +
                    "}\n" +
                    "@end\n");
  }

  public void testWithinProtocolsBrackets() throws Exception {
    prepareForFormat("@protocol Bar,Baz;\n" +
                     "@interface Foo<Bar,Baz>{\n" +
                     "    id<Bar> i;\n" +
                     "}\n" +
                     "-(id<Bar,Baz>)init;\n" +
                     "@end\n" +
                     "@implementation Foo<Bar,Baz>\n" +
                     "@end\n");

    getOCSettings().SPACE_WITHIN_PROTOCOLS_BRACKETS = false;
    assertFormat("@protocol Bar,Baz;\n" +
                 "@interface Foo<Bar,Baz>{\n" +
                 "    id<Bar> i;\n" +
                 "}\n" +
                 "-(id<Bar,Baz>)init;\n" +
                 "@end\n" +
                 "@implementation Foo<Bar,Baz>\n" +
                 "@end\n");

    getOCSettings().SPACE_WITHIN_PROTOCOLS_BRACKETS = true;
    assertFormat("@protocol Bar,Baz;\n" +
                 "@interface Foo< Bar,Baz >{\n" +
                 "    id< Bar > i;\n" +
                 "}\n" +
                 "-(id< Bar,Baz >)init;\n" +
                 "@end\n" +
                 "@implementation Foo< Bar,Baz >\n" +
                 "@end\n");
  }

  public void testBeforeProtocolsBrackets() throws Exception {
    prepareForFormat("@protocol Bar,Baz;\n" +
                     "@interface Foo<Bar,Baz>{\n" +
                     "    id<Bar> i;\n" +
                     "}\n" +
                     "-(id<Bar,Baz>)init;\n" +
                     "@end\n" +
                     "@implementation Foo<Bar,Baz>\n" +
                     "@end\n");

    getOCSettings().SPACE_BEFORE_PROTOCOLS_BRACKETS = false;
    assertFormat("@protocol Bar,Baz;\n" +
                 "@interface Foo<Bar,Baz>{\n" +
                 "    id<Bar> i;\n" +
                 "}\n" +
                 "-(id<Bar,Baz>)init;\n" +
                 "@end\n" +
                 "@implementation Foo<Bar,Baz>\n" +
                 "@end\n");

    getOCSettings().SPACE_BEFORE_PROTOCOLS_BRACKETS = true;
    assertFormat("@protocol Bar,Baz;\n" +
                 "@interface Foo <Bar,Baz>{\n" +
                 "    id <Bar> i;\n" +
                 "}\n" +
                 "-(id <Bar,Baz>)init;\n" +
                 "@end\n" +
                 "@implementation Foo <Bar,Baz>\n" +
                 "@end\n");
  }

  public void testBeforeGenericsBrackets() throws Exception {
    prepareForFormat("@interface Parent;\n" +
                     "@interface Foo<Generic1,Generic2>:Parent{\n" +
                     "    Foo<Generic1,Generic2> i;\n" +
                     "}\n" +
                     "-(Foo<Generic1,Generic2>)init;\n" +
                     "@end\n" +
                     "@implementation Foo\n" +
                     "@end\n");

    assertFormat("@interface Parent;\n" +
                 "@interface Foo<Generic1,Generic2>:Parent{\n" +
                 "    Foo<Generic1,Generic2> i;\n" +
                 "}\n" +
                 "-(Foo<Generic1,Generic2>)init;\n" +
                 "@end\n" +
                 "@implementation Foo\n" +
                 "@end\n");
  }

  public void testBeforeGenericsBrackets2() throws Exception {
    prepareForFormat("@interface Parent;\n" +
                     "@interface Foo <Generic1,Generic2>:Parent{\n" +
                     "    Foo <Generic1,Generic2> i;\n" +
                     "}\n" +
                     "-(Foo <Generic1,Generic2>)init;\n" +
                     "@end\n" +
                     "@implementation Foo\n" +
                     "@end\n");

    assertFormat("@interface Parent;\n" +
                 "@interface Foo <Generic1,Generic2>:Parent{\n" +
                 "    Foo <Generic1,Generic2> i;\n" +
                 "}\n" +
                 "-(Foo <Generic1,Generic2>)init;\n" +
                 "@end\n" +
                 "@implementation Foo\n" +
                 "@end\n");
  }

  public void testWithinCategoryParentheses() throws Exception {
    prepareForFormat("@interface Foo(Bar){\n" +
                     "}\n" +
                     "@end\n" +
                     "@implementation Foo(Bar)\n" +
                     "@end\n");

    getOCSettings().SPACE_WITHIN_CATEGORY_PARENTHESES = false;
    assertFormat("@interface Foo(Bar){\n" +
                 "}\n" +
                 "@end\n" +
                 "@implementation Foo(Bar)\n" +
                 "@end\n");

    getOCSettings().SPACE_WITHIN_CATEGORY_PARENTHESES = true;
    assertFormat("@interface Foo( Bar ){\n" +
                 "}\n" +
                 "@end\n" +
                 "@implementation Foo( Bar )\n" +
                 "@end\n");
  }

  public void testBeforeCategoryParentheses() throws Exception {
    prepareForFormat("@interface Foo(Bar){\n" +
                     "}\n" +
                     "@end\n" +
                     "@implementation Foo(Bar)\n" +
                     "@end\n");

    getOCSettings().SPACE_BEFORE_CATEGORY_PARENTHESES = false;
    assertFormat("@interface Foo(Bar){\n" +
                 "}\n" +
                 "@end\n" +
                 "@implementation Foo(Bar)\n" +
                 "@end\n");

    getOCSettings().SPACE_BEFORE_CATEGORY_PARENTHESES = true;
    assertFormat("@interface Foo (Bar){\n" +
                 "}\n" +
                 "@end\n" +
                 "@implementation Foo (Bar)\n" +
                 "@end\n");
  }

  public void testWithinMessageSendStandard() throws Exception {
    prepareForFormat("@implementation Bar {\n" +
                     "}\n" +
                     "- (id)    from:  (int)n       with     :(int)k andWith  :  (void *)p {\n" +
                     "[[[Foo     create  :  s with :42] init: p\n" +
                     "                               andWith:i] perform];\n" +
                     "}\n" +
                     "@end");
    assertFormat("@implementation Bar{\n" +
                 "}\n" +
                 "-(id)from:(int)n with:(int)k andWith:(void*)p{\n" +
                 "    [[[Foo create:s with:42]init:p andWith:i]perform];\n" +
                 "}\n" +
                 "@end");
  }

  public void testWithinMessageSendBrackets() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i[];\n" +
                     "    i[1];\n" +
                     "    [[NSObject alloc]init];\n" +
                     "}\n");

    getOCSettings().SPACE_BETWEEN_ADJACENT_BRACKETS = true;
    getOCSettings().SPACE_WITHIN_SEND_MESSAGE_BRACKETS = false;
    assertFormat("void foo(){\n" +
                 "    int i[];\n" +
                 "    i[1];\n" +
                 "    [[NSObject alloc]init];\n" +
                 "}\n");

    getOCSettings().SPACE_WITHIN_SEND_MESSAGE_BRACKETS = true;
    assertFormat("void foo(){\n" +
                 "    int i[];\n" +
                 "    i[1];\n" +
                 "    [ [ NSObject alloc ]init ];\n" +
                 "}\n");
  }


  public void testBeforeChainedMessageSend() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    [[NSObject alloc]init];\n" +
                     "}\n");

    getOCSettings().SPACE_BEFORE_CHAINED_SEND_MESSAGE = false;
    assertFormat("void foo(){\n" +
                 "    [[NSObject alloc]init];\n" +
                 "}\n");

    getOCSettings().SPACE_BEFORE_CHAINED_SEND_MESSAGE = true;
    assertFormat("void foo(){\n" +
                 "    [[NSObject alloc] init];\n" +
                 "}\n");
  }

  public void testWithinBraces() throws Exception {
    prepareForFormatCPP(
      """class A{};
        |void foo(){}
        |void foo(){{return ^{return;};}}
        |void *block = ^{};
        |auto la = [](){};
        |int i[] = {};""".stripMargin());

    getOCSettings().KEEP_STRUCTURES_IN_ONE_LINE = true;
    getCommonSettings().KEEP_SIMPLE_METHODS_IN_ONE_LINE = true;
    getCommonSettings().KEEP_SIMPLE_LAMBDAS_IN_ONE_LINE = true;
    getCommonSettings().KEEP_SIMPLE_BLOCKS_IN_ONE_LINE = true;

    getOCSettings().SPACE_BETWEEN_ADJACENT_BRACKETS = true;
    getCommonSettings().SPACE_WITHIN_BRACES = false;
    assertFormat(
      """class A{};
        |void foo(){}
        |void foo(){{return ^{return;};}}
        |void*block=^{};
        |auto la=[](){};
        |int i[]={};""".stripMargin());

    getCommonSettings().SPACE_WITHIN_BRACES = true;
    assertFormat(
      """class A{};
        |void foo(){}
        |void foo(){ { return ^{ return; }; } }
        |void*block=^{};
        |auto la=[](){};
        |int i[]={};""".stripMargin());

    getOCSettings().SPACE_BETWEEN_ADJACENT_BRACKETS = false;
    assertFormat(
      """class A{};
        |void foo(){}
        |void foo(){{ return ^{ return; }; }}
        |void*block=^{};
        |auto la=[](){};
        |int i[]={};""".stripMargin());

    getOCSettings().SPACE_WITHIN_EMPTY_BRACES = true;
    assertFormat(
      """class A{ };
        |void foo(){ }
        |void foo(){{ return ^{ return; }; }}
        |void*block=^{ };
        |auto la=[](){ };
        |int i[]={};""".stripMargin());

    getCommonSettings().SPACE_WITHIN_EMPTY_ARRAY_INITIALIZER_BRACES = true;
    assertFormat(
      """class A{ };
        |void foo(){ }
        |void foo(){{ return ^{ return; }; }}
        |void*block=^{ };
        |auto la=[ ](){ };
        |int i[]={ };""".stripMargin());
  }

  public void testWithinParentheses() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=(1+2)*3;\n" +
                     "    while(true);\n" +
                     "    for(int j=0;true;j++);\n" +
                     "}\n");

    getCommonSettings().SPACE_WITHIN_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    int i=(1+2)*3;\n" +
                 "    while(true);\n" +
                 "    for(int j=0;true;j++);\n" +
                 "}\n");

    getCommonSettings().SPACE_WITHIN_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    int i=( 1+2 )*3;\n" +
                 "    while(true);\n" +
                 "    for(int j=0;true;j++);\n" +
                 "}\n");
  }

  public void testWithinFunctionCall() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    foo();\n" +
                     "    bar(1);\n" +
                     "}\n");

    getOCSettings().SPACE_WITHIN_FUNCTION_CALL_PARENTHESES = false;

    getOCSettings().SPACE_WITHIN_EMPTY_FUNCTION_CALL_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    foo();\n" +
                 "    bar(1);\n" +
                 "}\n");

    getOCSettings().SPACE_WITHIN_EMPTY_FUNCTION_CALL_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    foo( );\n" +
                 "    bar(1);\n" +
                 "}\n");


    getOCSettings().SPACE_WITHIN_FUNCTION_CALL_PARENTHESES = true;

    getOCSettings().SPACE_WITHIN_EMPTY_FUNCTION_CALL_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    foo();\n" +
                 "    bar( 1 );\n" +
                 "}\n");

    getOCSettings().SPACE_WITHIN_EMPTY_FUNCTION_CALL_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    foo( );\n" +
                 "    bar( 1 );\n" +
                 "}\n");
  }

  public void testWithinFunctionAndBlockDeclarationCall() throws Exception {
    getOCSettings().SPACE_BETWEEN_ADJACENT_BRACKETS = true;

    prepareForFormat("void foo();\n" +
                     "void bar(void*(int),void(*)(),void*(*)()){\n" +
                     "    return ^(){\n" +
                     "    };\n" +
                     "    return ^(int i){\n" +
                     "    };\n" +
                     "}\n");

    getOCSettings().SPACE_WITHIN_FUNCTION_DECLARATION_PARENTHESES = false;
    getOCSettings().SPACE_WITHIN_EMPTY_FUNCTION_DECLARATION_PARENTHESES = false;
    assertFormat("void foo();\n" +
                 "void bar(void*(int),void(*)(),void*(*)()){\n" +
                 "    return ^(){\n" +
                 "    };\n" +
                 "    return ^(int i){\n" +
                 "    };\n" +
                 "}\n");
    getOCSettings().SPACE_WITHIN_EMPTY_FUNCTION_DECLARATION_PARENTHESES = true;
    assertFormat("void foo( );\n" +
                 "void bar(void*(int),void(*)( ),void*(*)( )){\n" +
                 "    return ^( ){\n" +
                 "    };\n" +
                 "    return ^(int i){\n" +
                 "    };\n" +
                 "}\n");

    getOCSettings().SPACE_WITHIN_FUNCTION_DECLARATION_PARENTHESES = true;
    getOCSettings().SPACE_WITHIN_EMPTY_FUNCTION_DECLARATION_PARENTHESES = false;
    assertFormat("void foo();\n" +
                 "void bar( void*( int ),void(*)(),void*(*)() ){\n" +
                 "    return ^(){\n" +
                 "    };\n" +
                 "    return ^( int i ){\n" +
                 "    };\n" +
                 "}\n");

    getOCSettings().SPACE_WITHIN_EMPTY_FUNCTION_DECLARATION_PARENTHESES = true;
    assertFormat("void foo( );\n" +
                 "void bar( void*( int ),void(*)( ),void*(*)( ) ){\n" +
                 "    return ^( ){\n" +
                 "    };\n" +
                 "    return ^( int i ){\n" +
                 "    };\n" +
                 "}\n");
  }

  public void testWithinIfParentheses() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    if(true);\n" +
                     "}\n");

    getCommonSettings().SPACE_WITHIN_IF_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    if(true);\n" +
                 "}\n");

    getCommonSettings().SPACE_WITHIN_IF_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    if( true );\n" +
                 "}\n");
  }

  public void testWithinForParentheses() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    for(int j=0;true;j++);\n" +
                     "    for(NSObject*o in nil);\n" +
                     "}\n");

    getCommonSettings().SPACE_WITHIN_FOR_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    for(int j=0;true;j++);\n" +
                 "    for(NSObject*o in nil);\n" +
                 "}\n");

    getCommonSettings().SPACE_WITHIN_FOR_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    for( int j=0;true;j++ );\n" +
                 "    for( NSObject*o in nil );\n" +
                 "}\n");
  }

  public void testWithinWhileParentheses() throws Exception {
    getCommonSettings().KEEP_CONTROL_STATEMENT_IN_ONE_LINE = true;
    prepareForFormat("void foo(){\n" +
                     "    while(true);\n" +
                     "    do;while(true);\n" +
                     "}\n");

    getCommonSettings().SPACE_WITHIN_WHILE_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    while(true);\n" +
                 "    do;while(true);\n" +
                 "}\n");

    getCommonSettings().SPACE_WITHIN_WHILE_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    while( true );\n" +
                 "    do;while( true );\n" +
                 "}\n");
  }

  public void testWithinSwitchParentheses() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    switch(1){\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_WITHIN_SWITCH_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    switch(1){\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_WITHIN_SWITCH_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    switch( 1 ){\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testWithinCatchParentheses() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    @try{\n" +
                     "    }@catch(NSException*exception){\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_WITHIN_CATCH_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    }@catch(NSException*exception){\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_WITHIN_CATCH_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    @try{\n" +
                 "    }@catch( NSException*exception ){\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testWithinSynchronizedParentheses() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    @synchronized(self){\n" +
                     "    }\n" +
                     "}\n");

    getCommonSettings().SPACE_WITHIN_SYNCHRONIZED_PARENTHESES = false;
    assertFormat("void foo(){\n" +
                 "    @synchronized(self){\n" +
                 "    }\n" +
                 "}\n");

    getCommonSettings().SPACE_WITHIN_SYNCHRONIZED_PARENTHESES = true;
    assertFormat("void foo(){\n" +
                 "    @synchronized( self ){\n" +
                 "    }\n" +
                 "}\n");
  }

  public void testWithinCastAndTypesParentheses() throws Exception {
    prepareForFormat("@interface Foo\n" +
                     "-(void*)foo:(void*)p;\n" +
                     "@end\n" +
                     "void foo(){\n" +
                     "    [o foo:(int)1];\n" +
                     "    void*p=(void*)0;\n" +
                     "}\n");

    getCommonSettings().SPACE_WITHIN_CAST_PARENTHESES = false;
    assertFormat("@interface Foo\n" +
                 "-(void*)foo:(void*)p;\n" +
                 "@end\n" +
                 "void foo(){\n" +
                 "    [o foo:(int)1];\n" +
                 "    void*p=(void*)0;\n" +
                 "}\n");

    getCommonSettings().SPACE_WITHIN_CAST_PARENTHESES = true;
    assertFormat("@interface Foo\n" +
                 "-(void*)foo:(void*)p;\n" +
                 "@end\n" +
                 "void foo(){\n" +
                 "    [o foo:( int )1];\n" +
                 "    void*p=( void* )0;\n" +
                 "}\n");

    getCommonSettings().SPACE_WITHIN_CAST_PARENTHESES = false;
    getOCSettings().SPACE_WITHIN_METHOD_RETURN_TYPE_PARENTHESES = false;
    assertFormat("@interface Foo\n" +
                 "-(void*)foo:(void*)p;\n" +
                 "@end\n" +
                 "void foo(){\n" +
                 "    [o foo:(int)1];\n" +
                 "    void*p=(void*)0;\n" +
                 "}\n");

    getOCSettings().SPACE_WITHIN_METHOD_RETURN_TYPE_PARENTHESES = true;
    assertFormat("@interface Foo\n" +
                 "-( void* )foo:(void*)p;\n" +
                 "@end\n" +
                 "void foo(){\n" +
                 "    [o foo:(int)1];\n" +
                 "    void*p=(void*)0;\n" +
                 "}\n");

    getOCSettings().SPACE_WITHIN_METHOD_RETURN_TYPE_PARENTHESES = false;
    getOCSettings().SPACE_WITHIN_METHOD_PARAMETER_TYPE_PARENTHESES = false;
    assertFormat("@interface Foo\n" +
                 "-(void*)foo:(void*)p;\n" +
                 "@end\n" +
                 "void foo(){\n" +
                 "    [o foo:(int)1];\n" +
                 "    void*p=(void*)0;\n" +
                 "}\n");

    getOCSettings().SPACE_WITHIN_METHOD_PARAMETER_TYPE_PARENTHESES = true;
    assertFormat("@interface Foo\n" +
                 "-(void*)foo:( void* )p;\n" +
                 "@end\n" +
                 "void foo(){\n" +
                 "    [o foo:(int)1];\n" +
                 "    void*p=(void*)0;\n" +
                 "}\n");
  }

  public void testWithinPropertyAttributesParens() throws Exception {
    prepareForFormat("@interface Foo\n" +
                     "@property(readonly,retain)int p;\n" +
                     "@end\n");

    getOCSettings().SPACE_WITHIN_PROPERTY_ATTRIBUTES_PARENTHESES = false;
    assertFormat("@interface Foo\n" +
                 "@property(readonly,retain) int p;\n" +
                 "@end\n");

    getOCSettings().SPACE_WITHIN_PROPERTY_ATTRIBUTES_PARENTHESES = true;
    assertFormat("@interface Foo\n" +
                 "@property( readonly,retain ) int p;\n" +
                 "@end\n");
  }

  public void testBeforeQuest() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=1?1:0;\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_QUEST = false;
    assertFormat("void foo(){\n" +
                 "    int i=1?1:0;\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_QUEST = true;
    assertFormat("void foo(){\n" +
                 "    int i=1 ?1:0;\n" +
                 "}\n");
  }

  public void _testBeforeQuestWithMacro() throws Exception {
    // todo
    prepareForFormat("void foo(){\n" +
                     "    BOOL b=TRUE?FALSE:TRUE;\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_QUEST = false;
    assertFormat("void foo(){\n" +
                 "    BOOL b=TRUE?FALSE:TRUE;\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_QUEST = true;
    assertFormat("void foo(){\n" +
                 "    BOOL b=TRUE ?FALSE :TRUE;\n" +
                 "}\n");
  }

  public void testAfterQuest() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=1?1:0;\n" +
                     "}\n");

    getCommonSettings().SPACE_AFTER_QUEST = false;
    assertFormat("void foo(){\n" +
                 "    int i=1?1:0;\n" +
                 "}\n");

    getCommonSettings().SPACE_AFTER_QUEST = true;
    assertFormat("void foo(){\n" +
                 "    int i=1? 1:0;\n" +
                 "}\n");
  }

  public void testBeforeColon() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=1?1:0;\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_COLON = false;
    assertFormat("void foo(){\n" +
                 "    int i=1?1:0;\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_COLON = true;
    assertFormat("void foo(){\n" +
                 "    int i=1?1 :0;\n" +
                 "}\n");
  }

  public void testAfterColon() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i=1?1:0;\n" +
                     "}\n");

    getCommonSettings().SPACE_AFTER_COLON = false;
    assertFormat("void foo(){\n" +
                 "    int i=1?1:0;\n" +
                 "}\n");

    getCommonSettings().SPACE_AFTER_COLON = true;
    assertFormat("void foo(){\n" +
                 "    int i=1?1: 0;\n" +
                 "}\n");
  }

  public void testBeforeComma() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i[]={1,2,3};\n" +
                     "    foo(1,2,3);\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_COMMA = false;
    assertFormat("void foo(){\n" +
                 "    int i[]={1,2,3};\n" +
                 "    foo(1,2,3);\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_COMMA = true;
    assertFormat("void foo(){\n" +
                 "    int i[]={1 ,2 ,3};\n" +
                 "    foo(1 ,2 ,3);\n" +
                 "}\n");
  }

  public void testAfterComma() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i,i[]={1,2,3};\n" +
                     "    foo(1,2,3);\n" +
                     "}\n");

    getCommonSettings().SPACE_AFTER_COMMA = false;
    assertFormat("void foo(){\n" +
                 "    int i,i[]={1,2,3};\n" +
                 "    foo(1,2,3);\n" +
                 "}\n");

    getCommonSettings().SPACE_AFTER_COMMA = true;
    assertFormat("void foo(){\n" +
                 "    int i, i[]={1, 2, 3};\n" +
                 "    foo(1, 2, 3);\n" +
                 "}\n");
  }

  public void testBeforeSemicolon() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i;\n" +
                     "    for(int i=0,j=0;true;i++);\n" +
                     "    for(;true;);\n" +
                     "}\n");

    getCommonSettings().SPACE_BEFORE_SEMICOLON = false;
    assertFormat("void foo(){\n" +
                 "    int i;\n" +
                 "    for(int i=0,j=0;true;i++);\n" +
                 "    for(;true;);\n" +
                 "}\n");

    getCommonSettings().SPACE_BEFORE_SEMICOLON = true;
    assertFormat("void foo(){\n" +
                 "    int i;\n" +
                 "    for(int i=0,j=0 ;true ;i++);\n" +
                 "    for(;true ;);\n" +
                 "}\n");
  }

  public void testAfterSemicolon() throws Exception {
    prepareForFormat("void foo(){\n" +
                     "    int i;\n" +
                     "    for(int i=0,j=0;true;i++);\n" +
                     "    for(;true;);\n" +
                     "}\n");

    getCommonSettings().SPACE_AFTER_SEMICOLON = false;
    assertFormat("void foo(){\n" +
                 "    int i;\n" +
                 "    for(int i=0,j=0;true;i++);\n" +
                 "    for(;true;);\n" +
                 "}\n");

    getCommonSettings().SPACE_AFTER_SEMICOLON = true;
    assertFormat("void foo(){\n" +
                 "    int i;\n" +
                 "    for(int i=0,j=0; true; i++);\n" +
                 "    for(; true;);\n" +
                 "}\n");
  }

  public void testAfterCast() throws Exception {
    prepareForFormat("@interface Foo\n" +
                     "-(void)foo:(void)p;\n" +
                     "@end\n" +
                     "void foo(void*(),void(*)(),void*(*)());\n" +
                     "void foo(){\n" +
                     "    [o foo:(int)1];\n" +
                     "    void*p=(void)0;\n" +
                     "}\n");

    getCommonSettings().SPACE_AFTER_TYPE_CAST = false;
    assertFormat("@interface Foo\n" +
                 "-(void)foo:(void)p;\n" +
                 "@end\n" +
                 "void foo(void*(),void(*)(),void*(*)());\n" +
                 "void foo(){\n" +
                 "    [o foo:(int)1];\n" +
                 "    void*p=(void)0;\n" +
                 "}\n");

    getCommonSettings().SPACE_AFTER_TYPE_CAST = true;
    assertFormat("@interface Foo\n" +
                 "-(void)foo:(void)p;\n" +
                 "@end\n" +
                 "void foo(void*(),void(*)(),void*(*)());\n" +
                 "void foo(){\n" +
                 "    [o foo:(int) 1];\n" +
                 "    void*p=(void) 0;\n" +
                 "}\n");

    getCommonSettings().SPACE_AFTER_TYPE_CAST = false;

    getOCSettings().SPACE_AFTER_METHOD_RETURN_TYPE_PARENTHESES = false;
    assertFormat("@interface Foo\n" +
                 "-(void)foo:(void)p;\n" +
                 "@end\n" +
                 "void foo(void*(),void(*)(),void*(*)());\n" +
                 "void foo(){\n" +
                 "    [o foo:(int)1];\n" +
                 "    void*p=(void)0;\n" +
                 "}\n");

    getOCSettings().SPACE_AFTER_METHOD_RETURN_TYPE_PARENTHESES = true;
    assertFormat("@interface Foo\n" +
                 "-(void) foo:(void)p;\n" +
                 "@end\n" +
                 "void foo(void*(),void(*)(),void*(*)());\n" +
                 "void foo(){\n" +
                 "    [o foo:(int)1];\n" +
                 "    void*p=(void)0;\n" +
                 "}\n");

    getOCSettings().SPACE_AFTER_METHOD_RETURN_TYPE_PARENTHESES = false;
    getOCSettings().SPACE_AFTER_METHOD_PARAMETER_TYPE_PARENTHESES = false;
    assertFormat("@interface Foo\n" +
                 "-(void)foo:(void)p;\n" +
                 "@end\n" +
                 "void foo(void*(),void(*)(),void*(*)());\n" +
                 "void foo(){\n" +
                 "    [o foo:(int)1];\n" +
                 "    void*p=(void)0;\n" +
                 "}\n");

    getOCSettings().SPACE_AFTER_METHOD_PARAMETER_TYPE_PARENTHESES = true;
    assertFormat("@interface Foo\n" +
                 "-(void)foo:(void) p;\n" +
                 "@end\n" +
                 "void foo(void*(),void(*)(),void*(*)());\n" +
                 "void foo(){\n" +
                 "    [o foo:(int)1];\n" +
                 "    void*p=(void)0;\n" +
                 "}\n");
  }

  public void testBeforeSuperclassColon() throws Exception {
    prepareForFormat("@interface Foo:Bar{\n" +
                     "}\n" +
                     "@end\n");

    getOCSettings().SPACE_BEFORE_SUPERCLASS_COLON = false;
    assertFormat("@interface Foo:Bar{\n" +
                 "}\n" +
                 "@end\n");

    getOCSettings().SPACE_BEFORE_SUPERCLASS_COLON = true;
    assertFormat("@interface Foo :Bar{\n" +
                 "}\n" +
                 "@end\n");
  }

  public void testAfterSuperclassColon() throws Exception {
    prepareForFormat("@interface Foo:Bar{\n" +
                     "}\n" +
                     "@end\n");

    getOCSettings().SPACE_AFTER_SUPERCLASS_COLON = false;
    assertFormat("@interface Foo:Bar{\n" +
                 "}\n" +
                 "@end\n");

    getOCSettings().SPACE_AFTER_SUPERCLASS_COLON = true;
    assertFormat("@interface Foo: Bar{\n" +
                 "}\n" +
                 "@end\n");
  }

  public void testBeforeCppBaseClauseColon() throws Exception {
    prepareForFormatCPP("class Foo:Bar{\n" +
                        "};\n");

    getOCSettings().SPACE_BEFORE_SUPERCLASS_COLON = false;
    assertFormat("class Foo:Bar{\n" +
                 "};\n");

    getOCSettings().SPACE_BEFORE_SUPERCLASS_COLON = true;
    assertFormat("class Foo :Bar{\n" +
                 "};\n");
  }

  public void testAfterCppBaseClauseColon() throws Exception {
    prepareForFormatCPP("class Foo:Bar{\n" +
                        "};\n");

    getOCSettings().SPACE_AFTER_SUPERCLASS_COLON = false;
    assertFormat("class Foo:Bar{\n" +
                 "};\n");

    getOCSettings().SPACE_AFTER_SUPERCLASS_COLON = true;
    assertFormat("class Foo: Bar{\n" +
                 "};\n");
  }


  public void testAfterColonInSelector() throws Exception {
    prepareForFormat("@implementation Foo\n" +
                     "-(void)foo:(int)i{\n" +
                     "    [self bar:i];\n" +
                     "    return @selector(foo:bar:);\n" +
                     "}\n" +
                     "@end\n");

    getOCSettings().SPACE_AFTER_COLON_IN_SELECTOR = false;
    assertFormat("@implementation Foo\n" +
                 "-(void)foo:(int)i{\n" +
                 "    [self bar:i];\n" +
                 "    return @selector(foo:bar:);\n" +
                 "}\n" +
                 "@end\n");

    getOCSettings().SPACE_AFTER_COLON_IN_SELECTOR = true;
    assertFormat("@implementation Foo\n" +
                 "-(void)foo: (int)i{\n" +
                 "    [self bar: i];\n" +
                 "    return @selector(foo:bar:);\n" +
                 "}\n" +
                 "@end\n");
  }

  public void testAfterVisibilitySignInMethodDeclaration() throws Exception {
    prepareForFormat("@interface Foo\n" +
                     "-(void)foo;\n" +
                     "@end\n" +
                     "@implementation Foo\n" +
                     "+(void)foo{\n" +
                     "}\n" +
                     "@end\n");

    getOCSettings().SPACE_AFTER_VISIBILITY_SIGN_IN_METHOD_DECLARATION = false;
    assertFormat("@interface Foo\n" +
                 "-(void)foo;\n" +
                 "@end\n" +
                 "@implementation Foo\n" +
                 "+(void)foo{\n" +
                 "}\n" +
                 "@end\n");

    getOCSettings().SPACE_AFTER_VISIBILITY_SIGN_IN_METHOD_DECLARATION = true;
    assertFormat("@interface Foo\n" +
                 "- (void)foo;\n" +
                 "@end\n" +
                 "@implementation Foo\n" +
                 "+ (void)foo{\n" +
                 "}\n" +
                 "@end\n");
  }

  public void testAfterCupInBlocks() throws Exception {
    prepareForFormat("typedef void(^block)(char*);\n" +
                     "void foo(){\n" +
                     "    return ^{\n" +
                     "    };\n" +
                     "    return ^(int i){\n" +
                     "    };\n" +
                     "    return ^void(int i){\n" +
                     "    };\n" +
                     "}\n");

    getOCSettings().SPACE_AFTER_CUP_IN_BLOCKS = false;
    assertFormat("typedef void(^block)(char*);\n" +
                 "void foo(){\n" +
                 "    return ^{\n" +
                 "    };\n" +
                 "    return ^(int i){\n" +
                 "    };\n" +
                 "    return ^void(int i){\n" +
                 "    };\n" +
                 "}\n");

    getOCSettings().SPACE_AFTER_CUP_IN_BLOCKS = true;
    assertFormat("typedef void(^ block)(char*);\n" +
                 "void foo(){\n" +
                 "    return ^ {\n" +
                 "    };\n" +
                 "    return ^ (int i){\n" +
                 "    };\n" +
                 "    return ^ void(int i){\n" +
                 "    };\n" +
                 "}\n");
  }

  public void testBetweenAdjacentBrackets() throws Exception {
    prepareForFormat("@implementation Foo(Bar)\n" +
                     "-(void)x{\n" +
                     "    [[Foo x:[Foo y:(1)]]];\n" +
                     "    f((int)x);\n" +
                     "    int i=((1+1)*2);\n" +
                     "    int j=a[b[1]];\n" +
                     "    int i[]={1,2,{3}};\n" +
                     "}\n" +
                     "@end\n");

    getCommonSettings().SPACE_WITHIN_PARENTHESES = true;
    getCommonSettings().SPACE_WITHIN_BRACKETS = true;
    getCommonSettings().SPACE_WITHIN_ARRAY_INITIALIZER_BRACES = true;
    getCommonSettings().SPACE_WITHIN_CAST_PARENTHESES = true;
    getOCSettings().SPACE_WITHIN_SEND_MESSAGE_BRACKETS = true;
    getOCSettings().SPACE_WITHIN_FUNCTION_CALL_PARENTHESES = true;

    getOCSettings().SPACE_BETWEEN_ADJACENT_BRACKETS = true;
    assertFormat("@implementation Foo(Bar)\n" +
                 "-(void)x{\n" +
                 "    [ [ Foo x:[ Foo y:( 1 ) ] ] ];\n" +
                 "    f( ( int )x );\n" +
                 "    int i=( ( 1+1 )*2 );\n" +
                 "    int j=a[ b[ 1 ] ];\n" +
                 "    int i[]={ 1,2,{ 3 } };\n" +
                 "}\n" +
                 "@end\n");

    getOCSettings().SPACE_BETWEEN_ADJACENT_BRACKETS = false;
    assertFormat("@implementation Foo(Bar)\n" +
                 "-(void)x{\n" +
                 "    [[ Foo x:[ Foo y:( 1 ) ]]];\n" +
                 "    f(( int )x );\n" +
                 "    int i=(( 1+1 )*2 );\n" +
                 "    int j=a[ b[ 1 ]];\n" +
                 "    int i[]={ 1,2,{ 3 }};\n" +
                 "}\n" +
                 "@end\n");
  }

  public void testBeforeInitList() throws Exception {
    prepareForFormatCPP(
      """struct FooT{
        |   int m[3]{1,2,3};
        |   list<int> mem{1,2,3};
        |   list<int> mem1={1,2,3};
        |   list<int> *pmem=new list<int>{1,2,3};
        |   FooT():elems{-1,1}{
        |      list<int> _mem{1,2,3};
        |      list<int> _mem1={1,2,3};
        |      _pmem = new list<int>{1,2,3};
        |      // list<int>{ 1, 2, 3 }; not supported yet
        |   }
        |};""".stripMargin());

    assertFormat(
      """struct FooT{
        |    int m[3]{1,2,3};
        |    list<int> mem{1,2,3};
        |    list<int> mem1={1,2,3};
        |    list<int>*pmem=new list<int>{1,2,3};
        |    FooT():elems{-1,1}{
        |        list<int> _mem{1,2,3};
        |        list<int> _mem1={1,2,3};
        |        _pmem=new list<int>{1,2,3};
        |        // list<int>{ 1, 2, 3 }; not supported yet
        |    }
        |};""".stripMargin());

    getOCSettings().SPACE_BEFORE_INIT_LIST = true;
    assertFormat(
      """struct FooT{
        |    int m[3] {1,2,3};
        |    list<int> mem {1,2,3};
        |    list<int> mem1={1,2,3};
        |    list<int>*pmem=new list<int> {1,2,3};
        |    FooT():elems {-1,1}{
        |        list<int> _mem {1,2,3};
        |        list<int> _mem1={1,2,3};
        |        _pmem=new list<int> {1,2,3};
        |        // list<int>{ 1, 2, 3 }; not supported yet
        |    }
        |};""".stripMargin());
  }

  public void testColonInConstructorInitList() throws Exception {
    prepareForFormatCPP(
      """struct FooT{
        |    int m;
        |    FooT():m(0){}
        |};""".stripMargin());

    assertFormat(
      """struct FooT{
        |    int m;
        |    FooT():m(0){
        |    }
        |};""".stripMargin());

    getOCSettings().SPACE_BEFORE_INIT_LIST_COLON = true;
    assertFormat(
      """struct FooT{
        |    int m;
        |    FooT() :m(0){
        |    }
        |};""".stripMargin());

    getOCSettings().SPACE_AFTER_INIT_LIST_COLON = true;
    assertFormat(
      """struct FooT{
        |    int m;
        |    FooT() : m(0){
        |    }
        |};""".stripMargin());
  }

  public void testSpaceAroundKeywords() throws Exception {
    assertFormatCPP(
      """using   namespace   std;
        |const  int  SIZE  =  sizeof  1  +  1;""".stripMargin(),

      """using namespace std;
        |const int SIZE=sizeof 1+1;""".stripMargin());
  }

  public void testSmartTabIndentInSpaceIndent() throws Exception {
    getOCSettings().INDENT_NAMESPACE_MEMBERS = 4;
    getOCSettings().INDENT_CLASS_MEMBERS = 8;
    getOCSettings().INDENT_VISIBILITY_KEYWORDS = 4;
    assertFormatCPP(
      """namespace A{
        |  class A{
        |   private:
        |    void oop();
        |  };
        |}""".stripMargin(),

      """namespace A{
        |    class A{
        |        private:
        |            void oop();
        |    };
        |}""".stripMargin());

    CommonCodeStyleSettings.IndentOptions indentOptions = getCommonSettings().getIndentOptions()
    indentOptions.USE_TAB_CHARACTER = true;

    // here indentOptions.SMART_TABS is false. Indents should consist of tabs for 'true' and 'false'
    assertFormat(
      """namespace A{
        |\tclass A{
        |\t\tprivate:
        |\t\t\tvoid oop();
        |\t};
        |}""".stripMargin());

    indentOptions.SMART_TABS = true; // should work for both!
    // OC-6187
    assertFormat(
      """namespace A{
        |\tclass A{
        |\t\tprivate:
        |\t\t\tvoid oop();
        |\t};
        |}""".stripMargin());
  }

  public void testBeforeModuleImport() throws Exception {
    getCommonSettings().BLANK_LINES_BEFORE_IMPORTS = 2
    assertFormat(
      """int x;
        |@import Foundation;""".stripMargin(),

      """int x;
        |
        |
        |@import Foundation;""".stripMargin(),
    )

    assertFormat(
      """int x;@import Foundation;""".stripMargin(),

      """int x;
        |
        |
        |@import Foundation;""".stripMargin(),
      )
  }

  public void testAfterModuleImport() throws Exception {
    getCommonSettings().BLANK_LINES_AFTER_IMPORTS = 2
    assertFormat(
      """@import Foundation;@class MyClass;""".stripMargin(),

      """@import Foundation;
        |
        |
        |@class MyClass;""".stripMargin(),
      )
  }

  public void testBetweenDifferentImports() throws Exception {
    getCommonSettings().BLANK_LINES_AFTER_IMPORTS = 2
    assertFormat(
      """@import Foundation;
        |
        |#import <Foundation/Foundation.h>;""".stripMargin(),

      """@import Foundation;
        |#import <Foundation/Foundation.h>;""".stripMargin()
    )

    assertFormat(
      """#import <Foundation/Foundation.h>;
        |
        |@import Foundation;""".stripMargin(),

      """#import <Foundation/Foundation.h>;
        |@import Foundation;""".stripMargin()
    )
  }

  public void testRangeFormattingKeepsEmptyPsiElementsPosition() throws Exception {
    String initial =
      """@interface I1:NSObject<selection>-(void)method;</selection>
        |@end""".stripMargin()

    String expected =
      """@interface I1:NSObject
      |-(void)method;
      |@end""".stripMargin()

    PsiFile expectedPsiFile = myCodeInsightFixture.configureByText(expected, "expected.m")

    assertFormat(initial, expected, false, true)

    assertEquals(psiStructure(expectedPsiFile).replace("expected.m", "test.m"), psiStructure(getFile()))
  }
}

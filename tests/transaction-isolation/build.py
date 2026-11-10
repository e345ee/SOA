from pathlib import Path
import zipfile,subprocess,os,shutil
sources=Path(__file__).resolve().parent/'src/probe'
root=Path('second-service/target/transaction-probe'); root.mkdir(parents=True,exist_ok=True)
out=root/'classes'; out.mkdir(exist_ok=True)
cp='second-service/target/classes:'+':'.join(str(p) for p in Path('second-service/target/soa_second_service-1.0-SNAPSHOT/WEB-INF/lib').glob('*.jar'))+':'+str(Path.home()/'.m2/repository/jakarta/platform/jakarta.jakartaee-api/10.0.0/jakarta.jakartaee-api-10.0.0.jar')+':'+str(Path.home()/'.m2/repository/jakarta/data/jakarta.data-api/1.0.1/jakarta.data-api-1.0.1.jar')
subprocess.run([str(Path(os.environ['JAVA_HOME'])/'bin/javac') if os.environ.get('JAVA_HOME') else shutil.which('javac'),'-proc:none','-cp',cp,'-d',str(out),*map(str,sources.glob('*.java'))],check=True)
with zipfile.ZipFile('second-service/target/soa_second_service-1.0-SNAPSHOT.war') as src,zipfile.ZipFile(root/'probe.war','w') as dst:
 for name in src.namelist():
  if '/controller/' in name or name=='WEB-INF/glassfish-web.xml':continue
  dst.writestr(name,src.read(name))
 dst.writestr('WEB-INF/glassfish-web.xml','<glassfish-web-app><context-root>/transaction-check</context-root></glassfish-web-app>')
 for p in out.rglob('*.class'):dst.write(p,'WEB-INF/classes/'+str(p.relative_to(out)))

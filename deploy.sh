
#!/bin/bash
#cat keys/id_rsa.pub | ssh lab.70  "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys && chmod 700 ~/.ssh && chmod 600 ~/.ssh/authorized_keys" 
ssh lab.70 "mkdir -p /home/pi/NetBeansProjects/CoreGilbarcoEncore500/dist"
scp -r "./dist" lab.70:"/home/pi/NetBeansProjects/CoreGilbarcoEncore500"
ssh lab.70 "sleep 1 && sudo pm2 restart all"

cat keys/id_rsa.pub | ssh lab.71  "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys && chmod 700 ~/.ssh && chmod 600 ~/.ssh/authorized_keys" 
ssh lab.71 "mkdir -p /home/pi/NetBeansProjects/CoreGilbarcoEncore500/dist"
scp -r "./dist" lab.71:"/home/pi/NetBeansProjects/CoreGilbarcoEncore500"
ssh  lab.71 "sleep 1 && sudo pm2 restart all"
 

#!/bin/bash
ssh lab.72  "pm2 stop all" 
scp -rp ./dist/* lab.72:"C:/Devitech/core/CoreGilbarcoEnconre500/"
ssh lab.72 "pm2 restart all"

rm -rf "./dist"
#!/bin/sh
KEYS_PATH=${KEYS_PATH:-/root/.ssh}
PRIVATE_KEY=$KEYS_PATH/id_rsa
PUBLIC_KEY=${PRIVATE_KEY}.pub
UID=${UID:-1000}
GUI=${GUI:-1000}
HOST=$HOST

echo -e 'y\n' | ssh-keygen -q -t rsa -N '' -f $PRIVATE_KEY
chmod 700 $KEYS_PATH
chmod 644 $PUBLIC_KEY
chmod 600 $PRIVATE_KEY
chown -R $UID $KEYS_PATH
chgrp -R $GUI $KEYS_PATH
#ssh-keyscan -H $HOST > $KEYS_PATH/known_hosts 

echo "========= PUBLIC KEY ============"
cat $PUBLIC_KEY
echo "======= END PUBLIC KEY ========="

exit 0

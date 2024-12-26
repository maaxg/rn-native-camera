import React, {useRef} from 'react';

import NativeCamera, {NativeCameraProps} from './specs/NativeCamera';
import NativeCameraModule from './specs/NativeCameraModule';
import {Button, View} from 'react-native';

function App(): React.JSX.Element {
  const ref = useRef<any>(null);

  async function capture() {
    try {
      /* const abc = await NativeCameraModule.capture(); */
      const abc = await ref.current.capture();

      console.log('Capture', abc);
    } catch (e) {
      console.log('Error', e);
    }
  }
  return (
    <View style={{flex: 1}}>
      <NativeCamera
        ref={ref}
        style={{flex: 1, width: '100%', height: '100%'}}
        onCameraReady={event => {
          console.log('Camera Ready', event);
        }}
      />

      <Button title="Capture" onPress={capture} />
    </View>
  );
}

export default App;
